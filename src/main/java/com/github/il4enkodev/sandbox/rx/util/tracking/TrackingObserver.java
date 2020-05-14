package com.github.il4enkodev.sandbox.rx.util.tracking;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.disposables.DisposableHelper;

import java.util.concurrent.atomic.AtomicReference;

final class TrackingObserver<T> implements Disposable, Observer<T> {

    private final TrackingTicket ticket;
    private final Observer<? super T> downstream;
    private final AtomicReference<Disposable> upstream = new AtomicReference<>();

    TrackingObserver(TrackingTicket ticket, Observer<? super T> downstream) {
        this.downstream = downstream;
        this.ticket = ticket;
    }

    @Override
    public void onSubscribe(Disposable d) {
        if (DisposableHelper.setOnce(upstream, d)) {
            ticket.activate();
            downstream.onSubscribe(this);
        }
    }

    @Override
    public void onNext(T t) {
        downstream.onNext(t);
    }

    @Override
    public void onError(Throwable e) {
        downstream.onError(e);
        ticket.cancel();
    }

    @Override
    public void onComplete() {
        downstream.onComplete();
        ticket.cancel();
    }

    @Override
    public void dispose() {
        DisposableHelper.dispose(upstream);
        ticket.cancel();
    }

    @Override
    public boolean isDisposed() {
        return upstream.get().isDisposed();
    }
}
