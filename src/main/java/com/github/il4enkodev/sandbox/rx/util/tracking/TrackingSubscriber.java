package com.github.il4enkodev.sandbox.rx.util.tracking;

import io.reactivex.internal.subscriptions.SubscriptionHelper;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicReference;

final class TrackingSubscriber<T> implements Subscription, Subscriber<T> {

    private final TrackingTicket ticket;
    private final Subscriber<? super T> downstream;
    private final AtomicReference<Subscription> upstream;

    TrackingSubscriber(TrackingTicket ticket, Subscriber<? super T> downstream) {
        this.ticket = ticket;
        this.downstream = downstream;
        upstream = new AtomicReference<>();
    }

    @Override
    public void onSubscribe(Subscription s) {
        if (SubscriptionHelper.setOnce(upstream, s)) {
            ticket.activate();
            downstream.onSubscribe(this);
        }
    }

    @Override
    public void onNext(T t) {
        downstream.onNext(t);
    }

    @Override
    public void onError(Throwable t) {
        downstream.onError(t);
        ticket.cancel();
    }

    @Override
    public void onComplete() {
        downstream.onComplete();
        ticket.cancel();
    }

    @Override
    public void request(long n) {
        upstream.get().request(n);
    }

    @Override
    public void cancel() {
        SubscriptionHelper.cancel(upstream);
        ticket.cancel();
    }
}
