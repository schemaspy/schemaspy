package org.schemaspy.input.dbms.driverclass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.sql.Driver;

/**
 * Logs in case an error occurs.
 */
public class DcErrorLogged implements Driverclass {

    private final Logger logger;
    private final Driverclass origin;

    public DcErrorLogged(final Driverclass origin) {
        this(LoggerFactory.getLogger(MethodHandles.lookup().lookupClass()), origin);
    }

    public DcErrorLogged(final Logger logger, final Driverclass origin) {
        this.logger = logger;
        this.origin = origin;
    }

    @Override
    public Class<Driver> value() throws ClassNotFoundException {
        try {
            return this.origin.value();
        } catch (ClassNotFoundException e) {
            this.logger.debug("Unable to find driverClass '{}'", this.origin);
            throw e;
        }
    }
}
