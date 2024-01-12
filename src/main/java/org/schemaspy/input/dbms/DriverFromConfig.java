package org.schemaspy.input.dbms;

import java.sql.Driver;
import java.util.Properties;

import org.schemaspy.input.dbms.driver.Driversource;
import org.schemaspy.input.dbms.driverpath.DpFromIterable;
import org.schemaspy.input.dbms.driverpath.DpMissingPathChecked;


public final class DriverFromConfig implements Driversource {

    private final ConnectionConfig config;

    public DriverFromConfig(final ConnectionConfig config) {
        this.config = config;
    }

    @Override
    public Driver driver() {
        final Properties properties = this.config.getDatabaseTypeProperties();
        return new DbDriverLoader(
            properties.getProperty("driver").split(","),
            new DpMissingPathChecked(
                new DpFromIterable(this.config.getDriverPath())
            )
        ).driver();
    }
}
