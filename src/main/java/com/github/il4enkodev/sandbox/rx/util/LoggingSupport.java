package com.github.il4enkodev.sandbox.rx.util;

import org.slf4j.Logger;
import org.slf4j.event.Level;
import org.slf4j.spi.LocationAwareLogger;

import java.util.concurrent.atomic.AtomicInteger;

interface LoggingSupport {

    AtomicInteger counter();
    String name();
    Logger logger();
    Level level();

    default void subscribed() {
        log("{} has been subscribed to source", name());
    }

    default void unsubscribed() {
        log("{} has been unsubscribed from source", name());
    }

    default void next(Object next) {
        log("{} has received {} element: '{}'", name(), ordinal(counter().incrementAndGet()), next);
    }

    default void complete() {
        log("{} has been completed", name());
    }

    default void error(Throwable e) {
        log("{} has been completed with error", e, name());
    }

    default void log(String format, Object... args) {
        log(format, null, args);
    }

    default void log(String format, Throwable t, Object... args) {
        final LocationAwareLogger logger = (LocationAwareLogger) logger();
        logger.log(null, logger.getName(), level().toInt(), format, args, t);
    }

    static String ordinal(int i) {
        String[] suffixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
        switch (i % 100) {
            case 11:
            case 12:
            case 13:
                return i + "th";
            default:
                return i + suffixes[i % 10];
        }
    }
}
