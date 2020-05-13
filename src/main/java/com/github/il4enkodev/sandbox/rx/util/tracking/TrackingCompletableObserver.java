package com.github.il4enkodev.sandbox.rx.util.tracking;

import io.reactivex.CompletableObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.disposables.DisposableHelper;

import java.util.concurrent.atomic.AtomicReference;

final class TrackingCompletableObserver implements Disposable, CompletableObserver {

    private final TrackingTicket ticket;
    private final CompletableObserver downstream;
    private final AtomicReference<Disposable> upstream;

    TrackingCompletableObserver(TrackingTicket ticket, CompletableObserver downstream) {
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
    public void onComplete() {
        ticket.cancel();
        downstream.onComplete();
    }

    @Override
    public void onError(Throwable e) {
        ticket.cancel();
        downstream.onError(e);
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
