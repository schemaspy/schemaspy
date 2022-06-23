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

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;

/**
 * @author Nils Petzaell
 */
public class StackTraceOmitter extends ThrowableProxyConverter { // NOSONAR

	private static final Logger SCHEMA_SPY_LOGGER = LoggerFactory.getLogger("org.schemaspy");

	private static AtomicBoolean omittedStackTrace = new AtomicBoolean(false);

	public static boolean hasOmittedStackTrace() {
		return omittedStackTrace.get();
	}

	@Override
	public String convert(ILoggingEvent event) {
		if (SCHEMA_SPY_LOGGER.isDebugEnabled())
			return super.convert(event);
		if (Objects.nonNull(event.getThrowableProxy())) {
			omittedStackTrace.set(true);
			return event.getThrowableProxy().getMessage() + CoreConstants.LINE_SEPARATOR;
		}
		return CoreConstants.EMPTY_STRING;
	}

}
