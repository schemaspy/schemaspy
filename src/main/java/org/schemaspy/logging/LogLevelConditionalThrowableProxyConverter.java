package org.schemaspy.logging;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogLevelConditionalThrowableProxyConverter extends ThrowableProxyConverter {

    private static final Logger SCHEMA_SPY_LOGGER = LoggerFactory.getLogger("org.schemaspy");

    public static void register() {
        PatternLayout.defaultConverterMap.put("debugEx", LogLevelConditionalThrowableProxyConverter.class.getName());
    }

    @Override
    public String convert(ILoggingEvent event) {
        if (SCHEMA_SPY_LOGGER.isDebugEnabled())
            return super.convert(event);
        return CoreConstants.EMPTY_STRING;
    }
}
