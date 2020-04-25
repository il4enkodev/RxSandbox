package com.github.il4enkodev.sandbox.rx.log;

import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.joran.spi.ConsoleTarget;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.WarnStatus;
import org.fusesource.jansi.AnsiConsole;

import java.util.Arrays;

public class ColorizedAppender<E> extends OutputStreamAppender<E> {

    protected ConsoleTarget target = ConsoleTarget.SystemOut;

    @Override
    public void start() {
        if (!launchedFromIdea())
            AnsiConsole.systemInstall();
        setOutputStream(target.getStream());
        super.start();
    }

    public void setTarget(String value) {
        ConsoleTarget t = ConsoleTarget.findByName(value.trim());
        if (t == null) {
            targetWarn(value);
        } else {
            target = t;
        }
    }

    public String getTarget() {
        return target.getName();
    }

    private void targetWarn(String val) {
        Status status = new WarnStatus("[" + val + "] should be one of " + Arrays.toString(ConsoleTarget.values()), this);
        status.add(new WarnStatus("Using previously set target, System.out by default.", this));
        addStatus(status);
    }

    static boolean launchedFromIdea() {
        return System.getProperty("java.class.path").contains("idea_rt.jar");
    }
}
