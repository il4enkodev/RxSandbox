package com.github.il4enkodev.sandbox.rx.util;

import io.reactivex.disposables.Disposable;
import io.reactivex.internal.disposables.DisposableHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

final class LoggingObserver<T>
extends AtomicReference<Disposable>
implements LoggingSupport, DisposableCompletableObserver, DisposableMaybeObserver<T>,
        DisposableSingleObserver<T>, DisposableObserver<T> {

    private static final Logger logger = LoggerFactory.getLogger(LoggingObserver.class);

    private final Level level;
    private final String name;

    private final AtomicInteger counter = new AtomicInteger();

    LoggingObserver(Level level, String name) {
        this.level = level;
        this.name = name;
    }

    @Override
    public AtomicInteger counter() {
        return counter;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Logger logger() {
        return logger;
    }

    @Override
    public Level level() {
        return level;
    }

    void onDispose() {
        unsubscribed();
    }

    @Override
    public void onSubscribe(Disposable actual) {
        DisposableHelper.setOnce(this, actual);
        subscribed();
    }

    @Override
    public void onNext(T e) {
        next(e);
    }

    @Override
    public void onSuccess(T value) {
        log("{} complete with value: '{}'", name, value);
    }

    @Override
    public void onComplete() {
        complete();
    }

    @Override
    public void onError(Throwable e) {
        error(e);
    }

    @Override
    public void dispose() {
        if (DisposableHelper.dispose(this)) {
            onDispose();
        }
    }

    @Override
    public boolean isDisposed() {
        return get().isDisposed();
    }
}
