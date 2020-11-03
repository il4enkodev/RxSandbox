package com.github.il4enkodev.sandbox.rx;


import io.reactivex.functions.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

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
    }

    static void exit() {
        Main.scheduler().shutdown();
    }

    static void sleep(long timeout, TimeUnit unit) {
        try {
            unit.sleep(timeout);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
