package org.schemaspy.input.dbms.driverpath;

import java.util.Properties;

/**
 * Encapsulates what driver path to use based on properties.
 */
public final class DpProperties implements Driverpath {

    private final Properties properties;

    public DpProperties(Properties properties) {
        this.properties = properties;
    }

    @Override
    public String value() {
        return this.properties.getProperty("driverPath");
    }
}
