package com.github.il4enkodev.sandbox.rx.util.tracking;

import io.reactivex.MaybeObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.disposables.DisposableHelper;

import java.util.concurrent.atomic.AtomicReference;

final class TrackingMaybeObserver<T> implements Disposable, MaybeObserver<T> {

    private final TrackingTicket ticket;
    private final MaybeObserver<? super T> downstream;
    private final AtomicReference<Disposable> upstream;

    TrackingMaybeObserver(TrackingTicket ticket, MaybeObserver<? super T> downstream) {
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
        ticket.cancel();
        downstream.onSuccess(t);
    }

    @Override
    public void onError(Throwable e) {
        ticket.cancel();
        downstream.onError(e);
    }

    @Override
    public void onComplete() {
        ticket.cancel();
        downstream.onComplete();
    }

    @Override
    public void dispose() {
        ticket.cancel();
        DisposableHelper.dispose(upstream);
    }

    @Override
    public boolean isDisposed() {
        return DisposableHelper.isDisposed(upstream.get());
    }
}
