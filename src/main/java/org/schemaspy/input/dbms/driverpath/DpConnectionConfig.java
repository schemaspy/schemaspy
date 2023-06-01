package org.schemaspy.input.dbms.driverpath;

import org.schemaspy.input.dbms.ConnectionConfig;

/**
 * Encapsulates what driver path to use based on connection config.
 */
public final class DpConnectionConfig implements Driverpath {

    private final ConnectionConfig config;

    public DpConnectionConfig(final ConnectionConfig config) {
        this.config = config;
    }

    @Override
    public String value() {
        return this.config.getDriverPath();
    }
}
