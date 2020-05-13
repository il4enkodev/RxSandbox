package com.github.il4enkodev.sandbox.rx.util.tracking;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.disposables.DisposableHelper;

import java.util.concurrent.atomic.AtomicReference;

final class TrackingSingleObserver<T> implements Disposable, SingleObserver<T> {

    private final TrackingTicket ticket;
    private final SingleObserver<? super T> downstream;
    private final AtomicReference<Disposable> upstream;

    TrackingSingleObserver(TrackingTicket ticket, SingleObserver<? super T> downstream) {
        this.ticket = ticket;
        this.downstream = downstream;
        upstream = new AtomicReference<>();
    }

    @Override
    public void onSubscribe(Disposable d) {
        if (DisposableHelper.setOnce(upstream, d)) {
            ticket.activate();
            downstream.onSubscribe(this);
        }
    }

    @Override
    public void onSuccess(T t) {
        downstream.onSuccess(t);
        ticket.cancel();
    }

    @Override
    public void onError(Throwable e) {
        downstream.onError(e);
        ticket.cancel();
    }

    @Override
    public void dispose() {
        DisposableHelper.dispose(upstream);
        ticket.cancel();
    }

    @Override
    public boolean isDisposed() {
        return DisposableHelper.isDisposed(upstream.get());
    }
}
