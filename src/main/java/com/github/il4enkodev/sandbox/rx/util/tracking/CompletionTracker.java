package com.github.il4enkodev.sandbox.rx.util.tracking;

import io.reactivex.*;
import io.reactivex.plugins.RxJavaPlugins;
import org.reactivestreams.Subscriber;

import java.util.Objects;
import java.util.concurrent.Phaser;

public final class CompletionTracker extends Thread {

    public static void install(Runnable action) {
        Objects.requireNonNull(action, "action is null");

        final CompletionTracker service = new CompletionTracker(action);

        RxJavaPlugins.setOnCompletableSubscribe(service::onCompletableSubscribe);
        RxJavaPlugins.setOnMaybeSubscribe(service::onMaybeSubscribe);
        RxJavaPlugins.setOnSingleSubscribe(service::onSingleSubscribe);
        RxJavaPlugins.setOnObservableSubscribe(service::onObservableSubscribe);
        RxJavaPlugins.setOnFlowableSubscribe(service::onFlowableSubscribe);

        service.start();
    }

    private final Phaser phaser;
    private final Runnable action;

    private CompletionTracker(Runnable action) {
        this.phaser = new Phaser();
        this.action = action;
    }

    @Override
    public void run() {
        phaser.awaitAdvance(0);
        action.run();
    }

    CompletableObserver onCompletableSubscribe(Completable source, CompletableObserver observer) {
        return new TrackingCompletableObserver(ticket(), observer);
    }

    MaybeObserver<Object> onMaybeSubscribe(Maybe<Object> source, MaybeObserver<Object> observer) {
        return new TrackingMaybeObserver<>(ticket(), observer);
    }

    SingleObserver<Object> onSingleSubscribe(Single<Object> source, SingleObserver<Object> observer) {
        return new TrackingSingleObserver<>(ticket(), observer);
    }

    Observer<Object> onObservableSubscribe(Observable<Object> source, Observer<Object> observer) {
        return new TrackingObserver<>(ticket(), observer);
    }

    Subscriber<Object> onFlowableSubscribe(Flowable<Object> source, Subscriber<Object> subscriber) {
        return new TrackingSubscriber<>(ticket(), subscriber);
    }

    private TrackingTicket ticket() {
        return new TrackingTicket(phaser);
    }
}
