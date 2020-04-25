package com.github.il4enkodev.sandbox.rx;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    @SuppressWarnings("FieldCanBeLocal")
    private final String[] args;

    public Application(String[] args) {
        this.args = args;
    }

    @Override
    public void run() {
        logger.trace("Application started");
        // Sandbox here
    }
}
