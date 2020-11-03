package com.github.il4enkodev.sandbox.rx.util.tracking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

final class TrackingTicket {

    private static final Logger logger = LoggerFactory.getLogger(TrackingTicket.class);

    private final CountDownLatch latch;
    private final AtomicInteger counter;

    private final AtomicInteger state;
    private static final int CREATED = 1;
    private static final int ACTIVE = 2;
    private static final int CANCELED = 3;

    TrackingTicket(CountDownLatch latch, AtomicInteger counter) {
        this.counter = counter;
        this.latch = latch;
        state = new AtomicInteger(CREATED);
    }

    void activate() {
        if (state.compareAndSet(CREATED, ACTIVE) && Integer.MAX_VALUE == counter.incrementAndGet()) {
            throw new OutOfMemoryError();
        }
    }

    void cancel() {
        if (state.get() == CANCELED || state.compareAndSet(CREATED, CANCELED)) {
            return;
        }

        if (state.compareAndSet(ACTIVE, CANCELED) && counter.decrementAndGet() == 0) {
            latch.countDown();
        }
    }
}
