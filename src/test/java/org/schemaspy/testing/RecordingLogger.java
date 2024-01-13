package org.schemaspy.testing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.LegacyAbstractLogger;
import org.slf4j.helpers.MessageFormatter;

public class RecordingLogger extends LegacyAbstractLogger {

    private List<SimpleLogEvent> logEvents = new ArrayList<>();

    private final boolean recordAll;

    public RecordingLogger() {
        this(true);
    }

    public RecordingLogger(boolean recordAll) {
        this.recordAll = recordAll;
    }
    public boolean isTraceEnabled() {
        return this.recordAll;
    }

    public boolean isDebugEnabled() {
        return this.recordAll;
    }

    public boolean isInfoEnabled() {
        return this.recordAll;
    }

    public boolean isWarnEnabled() {
        return this.recordAll;
    }

    public boolean isErrorEnabled() {
        return this.recordAll;
    }

    @Override
    protected String getFullyQualifiedCallerName() {
        return null;
    }

    @Override
    protected void handleNormalizedLoggingCall(
        final Level level,
        final Marker marker,
        final String messagePattern,
        final Object[] arguments,
        final Throwable throwable
    ) {
        logEvents.add(new SimpleLogEvent(level, MessageFormatter.basicArrayFormat(messagePattern, arguments)));
    }

    public List<SimpleLogEvent> events() {
        return Collections.unmodifiableList(logEvents);
    }

    public String toString() {
        return logEvents.stream().map(SimpleLogEvent::message).collect(Collectors.joining("\n"));
    }
}
