package com.github.il4enkodev.sandbox.rx.util;

import io.reactivex.Flowable;
import io.reactivex.annotations.SchedulerSupport;
import io.reactivex.schedulers.Schedulers;

import java.util.Random;
import java.util.Scanner;

import static io.reactivex.annotations.SchedulerSupport.IO;
import static io.reactivex.annotations.SchedulerSupport.COMPUTATION;

public class Flowables {

    @SchedulerSupport(IO)
    public static Flowable<String> input() {
        return Flowable.<String, Scanner>generate(
                () -> new Scanner(System.in),
                (scanner, emitter) -> { emitter.onNext(scanner.nextLine()); },
                Scanner::close
        ).subscribeOn(Schedulers.io());
    }

    @SchedulerSupport(COMPUTATION)
    public static Flowable<Integer> random(int from, int to) {
        return Flowable.generate(Random::new, (random, emitter) -> {
            emitter.onNext(random.nextInt(to - from + 1) + from);
        });
    }
}
