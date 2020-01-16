package io.siddhi.distribution.core.util;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Util class to read the logs of test cases.
 */
public class UnitTestAppender extends AppenderSkeleton {
    private List<String> messages = new ArrayList<>();

    @Override
    protected void append(LoggingEvent loggingEvent) {
        messages.add(loggingEvent.getRenderedMessage());
    }

    @Override
    public void close() {
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

    public List<String> getMessages() {
        return messages;
    }
}
