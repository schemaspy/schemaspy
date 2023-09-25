package org.schemaspy.input.dbms.driver;

import org.schemaspy.input.dbms.exceptions.ConnectionFailure;

import java.sql.Driver;

/**
 * Encapsulates how to access a JDBC driver through class.
 */
public final class DsDriverClass implements Driversource {

    private final Class<Driver> driverClass;

    public DsDriverClass(final Class<Driver> driverClass) {
        this.driverClass = driverClass;
    }

    @Override
    public Driver driver() {
        try {
            // have to use deprecated method or we won't see messages generated by older drivers
            return this.driverClass.newInstance();
        } catch (Exception exc) {
            throw new ConnectionFailure(
                String.format("Failed to create driver from driver class '%s'.", this.driverClass),
                exc
            );
        }
    }
}
