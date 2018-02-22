package org.schemaspy.util;

import java.io.IOException;

public class NotRunningFromJarException extends RuntimeException {
    public NotRunningFromJarException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
