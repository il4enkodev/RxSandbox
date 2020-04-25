package com.github.il4enkodev.sandbox.rx.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase;

import static ch.qos.logback.core.pattern.color.ANSIConstants.*;

public class ColorizedEncoder extends PatternLayoutEncoder {

    @Override
    public void start() {
        PatternLayout patternLayout = new PatternLayout();
        patternLayout.setContext(context);
        patternLayout.setPattern(getPattern());
        patternLayout.setOutputPatternAsHeader(outputPatternAsHeader);
        patternLayout.getDefaultConverterMap()
                .put("highlight", ExtendedHighlightingCompositeConverter.class.getName());
        patternLayout.start();
        this.layout = patternLayout;
        super.start();
    }

    public static class ExtendedHighlightingCompositeConverter extends ForegroundCompositeConverterBase<ILoggingEvent> {

        @Override
        protected String getForegroundColorCode(ILoggingEvent event) {
            Level level = event.getLevel();
            switch (level.toInt()) {
                case Level.ERROR_INT:
                    return RED_FG;
                case Level.WARN_INT:
                    return YELLOW_FG;
                case Level.INFO_INT:
                    return BLUE_FG;
                case Level.DEBUG_INT:
                    return MAGENTA_FG;
                default:
                    return CYAN_FG;
            }
        }
    }
}
