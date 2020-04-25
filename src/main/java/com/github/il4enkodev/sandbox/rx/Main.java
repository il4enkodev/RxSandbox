package com.github.il4enkodev.sandbox.rx;

import com.github.il4enkodev.sandbox.rx.util.LoggingSubscribers;
import com.github.il4enkodev.sandbox.rx.util.RunBlockingScheduler;
import io.reactivex.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final RunBlockingScheduler scheduler = new RunBlockingScheduler();

    public static Scheduler scheduler() {
        return scheduler;
    }

    public static void main(String[] args) {

        logger.trace("main() started");
        Thread.currentThread().setName("MainThread");

        // triggered when there are no active Observers/Subscribers
        // created using factory method of LoggingSubscribers class
        LoggingSubscribers.runAfterAllFinished(() -> Main.scheduler().shutdown());

        // run event loop here until shutdown() called or MainThread is interrupted
        scheduler.start(new Application(args));

        logger.trace("main() finished");
    }
}
