package com.github.il4enkodev.sandbox.rx.util;

import io.reactivex.internal.subscriptions.SubscriptionHelper;
import io.reactivex.internal.util.EndConsumerHelper;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.slf4j.spi.LocationAwareLogger;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class LoggingSubscriber<T> implements DisposableSubscriber<T>, LoggingSupport {

    private static final LocationAwareLogger logger =
            (LocationAwareLogger) LoggerFactory.getLogger(LoggingSubscribers.class);

    private final Level level;
    private final String name;
    private final long requested;

    final AtomicReference<Subscription> upstream = new AtomicReference<>();
    private final AtomicInteger counter = new AtomicInteger();

    public LoggingSubscriber(Level level, String name, long requested) {
        this.level = level;
        this.name = name;
        this.requested = requested;
    }

    @Override
    public void onSubscribe(Subscription s) {
        if (EndConsumerHelper.setOnce(this.upstream, s, getClass())) {
            subscribed();
            request(requested);
        }
    }

    @Override
    public void onNext(T next) {
        next(next);
        request(1);
    }

    @Override
    public void onError(Throwable t) {
        error(t);
    }

    @Override
    public void onComplete() {
        complete();
    }

    @Override
    public void dispose() {
        if (SubscriptionHelper.cancel(upstream)) {
            unsubscribed();
        }
    }

    @Override
    public boolean isDisposed() {
        return upstream.get() == SubscriptionHelper.CANCELLED;
    }

    void request(long n) {
        upstream.get().request(n);
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
}
