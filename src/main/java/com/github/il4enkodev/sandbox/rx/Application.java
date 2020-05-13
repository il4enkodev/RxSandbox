package com.github.il4enkodev.sandbox.rx;


import io.reactivex.functions.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static com.github.il4enkodev.sandbox.rx.util.LoggingSubscribers.*;

public class Application implements Action {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    @SuppressWarnings("FieldCanBeLocal")
    private final String[] args;

    public Application(String[] args) {
        this.args = args;
    }

    @Override
    public void run() {
        logger.trace("Application started");

        // sandbox here

        logger.trace("Application finished");
    }

    static void exit() {
        Main.scheduler().shutdown();
    }

    static void sleep(TimeUnit unit, long timeout) {
        try {
            unit.sleep(timeout);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
