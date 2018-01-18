package org.schemaspy.input.db.driver;

import java.util.Properties;

public interface AdapterConfig {

    /**
     * @return whether or not configuration has a user specified
     */
    boolean hasUser();

    /**
     * @return configured user
     */
    String getUser();

    /**
     * @return whether or nor configuration has a password specified
     */
    boolean hasPassword();

    /**
     * @return configured password
     */
    String getPassword();

    /**
     * @return configured connection properties supplied to the Driver {@link java.sql.Driver}
     */
    Properties getConnectionProps();

    /**
     * @return connectionUrl or connectionString supplied to the Driver {@link java.sql.Driver}
     */
    String getConnectionUrl();
}
