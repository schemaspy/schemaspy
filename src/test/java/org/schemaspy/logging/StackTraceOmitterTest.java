/*
 * Copyright (C) 2018 Nils Petzaell
 *
 * This file is part of SchemaSpy.
 *
 * SchemaSpy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SchemaSpy. If not, see <http://www.gnu.org/licenses/>.
 */
package org.schemaspy.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Nils Petzaell
 */
class StackTraceOmitterTest {

    @Test
    void onlyOutputsMessage() {
        setHasOmittedStackTrace(false);
        Level original = setLogLevel(Level.INFO);
        ILoggingEvent loggingEvent = mock(ILoggingEvent.class);
        when(loggingEvent.getThrowableProxy()).thenReturn(new ThrowableProxy(new IOException("Exception message")));
        StackTraceOmitter stackTraceOmitter = new StackTraceOmitter();
        assertThat(
                stackTraceOmitter.convert(loggingEvent).trim()
        ).isEqualTo("Exception message");
        assertThat(StackTraceOmitter.hasOmittedStackTrace()).isTrue();
        setHasOmittedStackTrace(false);
        setLogLevel(original);
    }

    @Test
    void outputExceptionIfDebug() {
        setHasOmittedStackTrace(false);
        Level orignal = setLogLevel(Level.DEBUG);
        ILoggingEvent loggingEvent = mock(ILoggingEvent.class);
        when(loggingEvent.getThrowableProxy()).thenReturn(new ThrowableProxy(new IOException("Exception message")));
        StackTraceOmitter stackTraceOmitter = new StackTraceOmitter();
        assertThat(
                stackTraceOmitter.convert(loggingEvent).trim()
        ).isEqualTo("java.io.IOException: Exception message");
        assertThat(StackTraceOmitter.hasOmittedStackTrace()).isFalse();
        setHasOmittedStackTrace(false);
        setLogLevel(orignal);
    }

    private void setHasOmittedStackTrace(boolean value) {
        try {
            Field field = StackTraceOmitter.class.getDeclaredField("omittedStackTrace");
            field.setAccessible(true);
            AtomicBoolean hasOmittedStacktrace = (AtomicBoolean) field.get(null);
            hasOmittedStacktrace.set(value);
            field.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Level setLogLevel(Level level) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = loggerContext.getLogger("org.schemaspy");
        Level original = logger.getLevel();
        logger.setLevel(level);
        return original;
    }
}
