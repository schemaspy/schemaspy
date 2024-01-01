package org.schemaspy.input.dbms;

import org.schemaspy.input.dbms.driver.Driversource;
import org.schemaspy.input.dbms.driverpath.DpConnectionConfig;
import org.schemaspy.input.dbms.driverpath.DpFallback;
import org.schemaspy.input.dbms.driverpath.DpMissingPathChecked;
import org.schemaspy.input.dbms.driverpath.DpNull;

import java.sql.Driver;
import java.util.Properties;


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
                new DpFallback(
                    new DpConnectionConfig(this.config),
                    new DpNull()
                )
            )
        ).driver();
    }
}
