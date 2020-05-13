package com.github.il4enkodev.sandbox.rx.util;

import io.reactivex.CompletableObserver;
import io.reactivex.MaybeObserver;
import io.reactivex.Observer;
import io.reactivex.SingleObserver;
import io.reactivex.internal.functions.ObjectHelper;
import org.reactivestreams.Subscriber;
import org.slf4j.event.Level;

import java.util.concurrent.atomic.AtomicInteger;

public final class LoggingSubscribers {

    private static final long DEFAULT_REQUEST = 1;

    private LoggingSubscribers() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static DisposableCompletableObserver newCompletableObserver() {
        return newCompletableObserver(generateNameFor(CompletableObserver.class));
    }

    public static <T> DisposableMaybeObserver<T> newMaybeObserver() {
        return newMaybeObserver(generateNameFor(MaybeObserver.class));
    }

    public static <T> DisposableSingleObserver<T> newSingleObserver() {
        return newSingleObserver(generateNameFor(SingleObserver.class));
    }

    public static <T> DisposableObserver<T> newObserver() {
        return newObserver(generateNameFor(Observer.class));
    }

    public static DisposableCompletableObserver newCompletableObserver(Level level) {
        return newCompletableObserver(level, generateNameFor(CompletableObserver.class));
    }

    public static <T> DisposableMaybeObserver<T> newMaybeObserver(Level level) {
        return newMaybeObserver(level, generateNameFor(MaybeObserver.class));
    }

    public static <T> DisposableSingleObserver<T> newSingleObserver(Level level) {
        return newSingleObserver(level, generateNameFor(SingleObserver.class));
    }

    public static <T> DisposableObserver<T> newObserver(Level level) {
        return newObserver(level, generateNameFor(Observer.class));
    }

    public static DisposableCompletableObserver newCompletableObserver(String name) {
        return newCompletableObserver(Level.DEBUG, name);
    }

    public static <T> DisposableMaybeObserver<T> newMaybeObserver(String name) {
        return newMaybeObserver(Level.DEBUG, name);
    }

    public static <T> DisposableSingleObserver<T> newSingleObserver(String name) {
        return newSingleObserver(Level.DEBUG, name);
    }

    public static <T> DisposableObserver<T> newObserver(String name) {
        return newObserver(Level.DEBUG, name);
    }

    public static DisposableCompletableObserver newCompletableObserver(Level level, String name) {
        return create(level, name);
    }

    public static <T> DisposableMaybeObserver<T> newMaybeObserver(Level level, String name) {
        return create(level, name);
    }

    public static <T> DisposableSingleObserver<T> newSingleObserver(Level level, String name) {
        return create(level, name);
    }

    public static <T> DisposableObserver<T> newObserver(Level level, String name) {
        return create(level, name);
    }

    public static <T> DisposableSubscriber<T> newSubscriber() {
        return newSubscriber(Level.DEBUG, generateNameFor(Subscriber.class), DEFAULT_REQUEST);
    }

    public static <T> DisposableSubscriber<T> newSubscriber(String name) {
        return newSubscriber(Level.DEBUG, name, DEFAULT_REQUEST);
    }

    public static <T> DisposableSubscriber<T> newSubscriber(Level level) {
        return newSubscriber(level, generateNameFor(Subscriber.class), DEFAULT_REQUEST);
    }

    public static <T> DisposableSubscriber<T> newSubscriber(Level level, String name) {
        return newSubscriber(level, name, DEFAULT_REQUEST);
    }

    public static <T> DisposableSubscriber<T> newSubscriber(Level level, String name, long requested) {
        return create(level, name, requested);
    }

    private static <T> DisposableSubscriber<T> create(Level level, String name, long requested) {
        validate(level, name, requested);
        //noinspection ResultOfMethodCallIgnored
        ObjectHelper.verifyPositive(requested, "requested");
        return new LoggingSubscriber<>(level, name, requested);
    }

    private static <T> LoggingObserver<T> create(Level level, String name) {
        validate(level, name);
        return new LoggingObserver<>(level, name);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void validate(Level level, String name, long requested) {
        validate(level, name);
        ObjectHelper.verifyPositive(requested, "requested");
    }

    private static void validate(Level level, String name) {
        if (level == null) {
            throw new NullPointerException("level is null");
        } else if (name == null) {
            throw new NullPointerException("name is null");
        } else if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("name is empty");
        }
    }

    private static final AtomicInteger OBSERVER_INDEX = new AtomicInteger();
    private static final AtomicInteger COMPLETABLE_OBSERVER_INDEX = new AtomicInteger();
    private static final AtomicInteger MAYBE_OBSERVER_INDEX = new AtomicInteger();
    private static final AtomicInteger SINGLE_OBSERVER_INDEX = new AtomicInteger();
    private static final AtomicInteger SUBSCRIBER_INDEX = new AtomicInteger();

    private static String generateNameFor(Class<?> type) {
        if (CompletableObserver.class.isAssignableFrom(type)) {
            return "CompletableObserver-" + COMPLETABLE_OBSERVER_INDEX.incrementAndGet();
        } else if (MaybeObserver.class.isAssignableFrom(type)) {
            return "MaybeObserver-" + MAYBE_OBSERVER_INDEX.incrementAndGet();
        } else if (SingleObserver.class.isAssignableFrom(type)) {
            return "SingleObserver-" + SINGLE_OBSERVER_INDEX.incrementAndGet();
        } else if (Observer.class.isAssignableFrom(type)) {
            return "Observer-" + OBSERVER_INDEX.incrementAndGet();
        } else if (Subscriber.class.isAssignableFrom(type)) {
            return "Subscriber-" + SUBSCRIBER_INDEX.incrementAndGet();
        } else throw new UnsupportedOperationException("name for " + type.getName());
    }
}
