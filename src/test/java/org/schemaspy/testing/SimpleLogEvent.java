package org.schemaspy.testing;

import org.slf4j.event.Level;

public record SimpleLogEvent(Level level, String message) { }
