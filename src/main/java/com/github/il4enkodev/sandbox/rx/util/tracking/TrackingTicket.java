package com.github.il4enkodev.sandbox.rx.util.tracking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicInteger;

final class TrackingTicket {

    private static final Logger logger = LoggerFactory.getLogger(TrackingTicket.class);

    private final Phaser phaser;
    private final AtomicInteger state;

    TrackingTicket(Phaser phaser) {
        this.phaser = phaser;
        state = new AtomicInteger();
    }

    void activate() {
        if (state.compareAndSet(0, 1)) {
            phaser.register();
            logger.trace("Ticket activated. {}", phaser);
        }
    }

    void cancel() {
        if (state.compareAndSet(1, 2)) {
            phaser.arrive();
            logger.trace("Ticket deactivated. {}", phaser);
        }
    }
}
