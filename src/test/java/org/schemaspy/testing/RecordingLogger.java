package org.schemaspy.testing;

import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.LegacyAbstractLogger;
import org.slf4j.helpers.MessageFormatter;

public class RecordingLogger extends LegacyAbstractLogger {

    private StringBuilder stringBuilder = new StringBuilder();

    private final boolean RECORD_ALL_EVENTS = true;
    public boolean isTraceEnabled() {
        return RECORD_ALL_EVENTS;
    }

    public boolean isDebugEnabled() {
        return RECORD_ALL_EVENTS;
    }

    public boolean isInfoEnabled() {
        return RECORD_ALL_EVENTS;
    }

    public boolean isWarnEnabled() {
        return RECORD_ALL_EVENTS;
    }

    public boolean isErrorEnabled() {
        return RECORD_ALL_EVENTS;
    }

    @Override
    protected String getFullyQualifiedCallerName() {
        return null;
    }

    @Override
    protected void handleNormalizedLoggingCall(Level level, Marker marker, String messagePattern, Object[] arguments, Throwable throwable) {
        stringBuilder.append(
                MessageFormatter.basicArrayFormat(messagePattern, arguments)
        );
    }

    public String toString() {
        return stringBuilder.toString();
    }
}
