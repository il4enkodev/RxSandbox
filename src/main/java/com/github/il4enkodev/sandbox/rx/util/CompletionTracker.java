package com.github.il4enkodev.sandbox.rx.util;

import com.github.il4enkodev.sandbox.rx.Main;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static io.reactivex.internal.functions.Functions.EMPTY_RUNNABLE;

final class CompletionTracker implements Consumer<CompletionTracker.Trackable> {

    private final AtomicReference<Runnable> state;
    private final Set<Trackable> tracked;

    CompletionTracker() {
        this.state = new AtomicReference<>(EMPTY_RUNNABLE);
        this.tracked = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    void add(Trackable t) {
        if (state.get() != null) {
            tracked.add(t);
            t.setCompletionTracker(this);
        }
    }

    @Override
    public void accept(Trackable trackable) {
        if (tracked.remove(trackable) && tracked.isEmpty()) {
            complete();
        }
    }

    private static final String TERMINATED = "Tracking has been terminated";
    private static final String ALREADY_SET = "Runnable already set";
    void setOnCompleteAction(Runnable action) {
        Objects.requireNonNull(action, "action is null");
        for (;;) {
            final Runnable current = state.get();
            if (current == null)
                throw new IllegalStateException(TERMINATED);

            if (current != EMPTY_RUNNABLE)
                throw new IllegalStateException(ALREADY_SET);

            if (state.compareAndSet(current, action))
                break;
        }
    }

    private void complete() {
        Runnable current = state.get();
        if (current != null) {
            current = state.getAndSet(null);
            if (current != null && current != EMPTY_RUNNABLE) {
                Main.scheduler().scheduleDirect(current);
            }
        }
    }

    interface Trackable {
        void setCompletionTracker(Consumer<Trackable> tracker);
    }
}
