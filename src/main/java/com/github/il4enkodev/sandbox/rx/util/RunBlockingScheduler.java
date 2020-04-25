package com.github.il4enkodev.sandbox.rx.util;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Cancellable;
import io.reactivex.internal.disposables.DisposableContainer;
import io.reactivex.internal.disposables.DisposableHelper;
import io.reactivex.internal.disposables.EmptyDisposable;
import io.reactivex.internal.functions.Functions;
import io.reactivex.plugins.RxJavaPlugins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static io.reactivex.internal.disposables.DisposableHelper.DISPOSED;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class RunBlockingScheduler extends Scheduler {

    private final TaskQueue queue = new TaskQueue();

    private static final Thread READY = new Thread("Scheduler has not started yet");
    private static final Thread SHUTDOWN = new Thread("Scheduler has been shutdown");
    private final AtomicReference<Thread> runner = new AtomicReference<>(READY);

    private static final Logger logger = LoggerFactory.getLogger(RunBlockingScheduler.class);

    @Override
    public Worker createWorker() {
        return new QueuingWorker();
    }

    public void start(Runnable initial) {
        Objects.requireNonNull(initial, "initial is null");
        final Thread current = Thread.currentThread();
        if (runner.compareAndSet(READY, current)) {
            try {
                initial.run();
                while (!current.isInterrupted()) {
                    queue.take().run();
                }
            } catch (InterruptedException ignore) {
            } finally {
                queue.clear();
                runner.set(SHUTDOWN);
            }
        } else if (runner.get() == SHUTDOWN){
            throw new IllegalStateException(SHUTDOWN.getName());
        } else {
            throw new IllegalStateException("Scheduler already running");
        }
    }

    @Override
    public void start() {
        start(Functions.EMPTY_RUNNABLE);
    }

    @Override
    public void shutdown() {
        Thread thread;
        for (;;) {
            thread = runner.get();
            if (thread == SHUTDOWN || thread == READY && runner.compareAndSet(READY, SHUTDOWN)) {
                break;
            }
            if (runner.compareAndSet(thread, SHUTDOWN)) {
                thread.interrupt();
                break;
            }
        }
    }

    @Override
    public Disposable scheduleDirect(Runnable run, long delay, TimeUnit unit) {
        final long executionTime = delay + now(unit);
        try {
            return enqueue(new QueuedTask(RxJavaPlugins.onSchedule(run), executionTime, unit, null));
        } catch (RejectedExecutionException e) {
            return EmptyDisposable.INSTANCE;
        }
    }

    Disposable enqueue(QueuedTask task) {
        final Thread state = runner.get();
        if (state == READY || state == SHUTDOWN) {
            logger.warn("Trying enqueue but {}", state.getName());
            throw new RejectedExecutionException();
        } else {
            queue.add(task);
            task.set(new TaskRemoval(queue, task));
            return task;
        }
    }

    static class TaskRemoval extends AtomicBoolean implements Disposable {

        final TaskQueue queue;
        final QueuedTask task;

        TaskRemoval(TaskQueue queue, QueuedTask task) {
            this.queue = queue;
            this.task = task;
        }

        @Override
        public void dispose() {
            if (compareAndSet(false, true)) {
                queue.remove(task);
            }
        }

        @Override
        public boolean isDisposed() {
            return get();
        }
    }

    class QueuingWorker extends Scheduler.Worker {

        final CompositeDisposable tasks = new CompositeDisposable();
        volatile boolean disposed;

        @Override
        public Disposable schedule(Runnable run, long delay, TimeUnit unit) {
            if (disposed) {
                return EmptyDisposable.INSTANCE;
            }

            Runnable wrappedRunnable = RxJavaPlugins.onSchedule(run);
            QueuedTask task = new QueuedTask(wrappedRunnable, delay + now(unit), unit, tasks);
            tasks.add(task);

            try {
                return enqueue(task);
            } catch (RejectedExecutionException e) {
                dispose();
                return EmptyDisposable.INSTANCE;
            }
        }

        @Override
        public void dispose() {
            if (!disposed) {
                disposed = true;
                tasks.dispose();
            }
        }

        @Override
        public boolean isDisposed() {
            return disposed || runner.get() == SHUTDOWN;
        }
    }

    static class QueuedTask extends AtomicReference<Disposable>
    implements Disposable, Runnable, Comparable<QueuedTask>, Cancellable {

        private static final Disposable RUNNING = EmptyDisposable.NEVER;

        int queueIndex = -1;
        final Runnable runnable;
        final long executionTime;
        final TimeUnit unit;
        final AtomicReference<DisposableContainer> parent;

        public QueuedTask(Runnable runnable, long executionTime, TimeUnit unit, DisposableContainer parent) {
            this.runnable = runnable;
            this.executionTime = executionTime;
            this.unit = unit;
            this.parent = new AtomicReference<>(parent);
        }

        @Override
        public void run() {
            try {
                if (DisposableHelper.replace(this, RUNNING)) {
                    runnable.run();
                }
            } catch (Throwable e) {
                RxJavaPlugins.onError(e);
            } finally {
                dispose();
            }
        }

        @Override
        public void cancel() {
            DisposableHelper.replace(this, DISPOSED);
            final DisposableContainer container = parent.getAndSet(null);
            if (container != null) {
                container.delete(this);
            }
        }

        @Override
        public void dispose() {
            DisposableHelper.dispose(this);
            final DisposableContainer container = parent.getAndSet(null);
            if (container != null) {
                container.delete(this);
            }
        }

        @Override
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(get());
        }

        @Override
        public int compareTo(QueuedTask that) {
            return Long.compare(this.getExecutionTime(MILLISECONDS), that.getExecutionTime(MILLISECONDS));
        }

        public long getExecutionTime(TimeUnit unit) {
            return unit.convert(executionTime, this.unit);
        }
    }

    static final class TaskQueue {

        private final ReentrantLock lock = new ReentrantLock();
        private final Condition available = lock.newCondition();
        private QueuedTask[] queue = new QueuedTask[16];
        private int size;

        public void add(QueuedTask t) {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                int i = size;
                if (i >= queue.length)
                    grow();
                size = i + 1;
                if (i == 0) {
                    queue[0] = t;
                    t.queueIndex = 0;
                } else {
                    siftUp(i, t);
                }
                if (queue[0] == t) {
                    available.signal();
                }
            } finally {
                lock.unlock();
            }
        }

        public void remove(QueuedTask t) {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                int i = indexOf(t);
                if (i < 0)
                    return;

                queue[i].queueIndex = -1;
                int s = --size;
                QueuedTask replacement = queue[s];
                queue[s] = null;
                if (s != i) {
                    siftDown(i, replacement);
                    if (queue[i] == replacement)
                        siftUp(i, replacement);
                }
            } finally {
                lock.unlock();
            }
        }

        public QueuedTask take() throws InterruptedException {
            final ReentrantLock lock = this.lock;
            lock.lockInterruptibly();
            try {
                for (;;) {
                    QueuedTask first = queue[0];
                    if (first == null)
                        available.await();
                    else {
                        long delay = first.getExecutionTime(MILLISECONDS) - System.currentTimeMillis();
                        if (delay <= 0)
                            return finishPoll(first);

                        first = null; // don't retain ref while waiting
                        available.await(delay, MILLISECONDS);
                    }
                }
            } finally {
                lock.unlock();
            }
        }

        public void clear() {
            QueuedTask[] tasks;
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                tasks = Arrays.copyOf(queue, size);
                Arrays.fill(queue, 0, size, null);
            } finally {
                lock.unlock();
            }

            cancel(tasks);
        }

        void cancel(QueuedTask[] array) {
            if (array != null) {
                for (QueuedTask task : array) {
                    task.queueIndex = -1;
                    task.cancel();
                }
            }
        }

        private void grow() {
            int oldCapacity = queue.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1); // grow 50%
            if (newCapacity < 0) // overflow
                newCapacity = Integer.MAX_VALUE;
            queue = Arrays.copyOf(queue, newCapacity);
        }

        private int indexOf(QueuedTask t) {
            if (t != null) {
                int i = t.queueIndex;
                if (i >= 0 && i < size && queue[i] == t)
                    return i;
            }
            return -1;
        }

        private void siftUp(int k, QueuedTask key) {
            while (k > 0) {
                int parent = (k - 1) >>> 1;
                QueuedTask e = queue[parent];
                if (key.compareTo(e) >= 0)
                    break;
                queue[k] = e;
                e.queueIndex = k;
                k = parent;
            }
            queue[k] = key;
            key.queueIndex = k;
        }

        private void siftDown(int k, QueuedTask key) {
            int half = size >>> 1;
            while (k < half) {
                int child = (k << 1) + 1;
                QueuedTask c = queue[child];
                int right = child + 1;
                if (right < size && c.compareTo(queue[right]) > 0)
                    c = queue[child - right];
                if (key.compareTo(c) <= 0)
                    break;
                queue[k] = c;
                c.queueIndex = k;
                k = child;
            }
            queue[k] = key;
            key.queueIndex = k;
        }

        private QueuedTask finishPoll(QueuedTask value) {
            int s = --size;
            QueuedTask x = queue[s];
            queue[s] = null;
            if (s != 0)
                siftDown(0, x);
            value.queueIndex = -1;
            return value;
        }
    }
}
