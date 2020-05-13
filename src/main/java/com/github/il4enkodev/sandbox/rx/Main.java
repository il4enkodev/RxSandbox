package com.github.il4enkodev.sandbox.rx;

import com.github.il4enkodev.sandbox.rx.util.tracking.CompletionTracker;
import hu.akarnokd.rxjava2.schedulers.BlockingScheduler;
import io.reactivex.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final BlockingScheduler scheduler = new BlockingScheduler();

    public static Scheduler scheduler() {
        return scheduler;
    }

    public static void main(String[] args) {

        logger.trace("main() started");
        Thread.currentThread().setName("MainThread");

        // triggered when there are no active Observers/Subscribers
        CompletionTracker.install(() -> Main.scheduler().shutdown());

        // run event loop here until shutdown() is called or MainThread is interrupted
        scheduler.execute(new Application(args));

        logger.trace("main() finished");
    }
}
