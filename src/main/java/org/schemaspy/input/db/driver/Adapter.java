package org.schemaspy.input.db.driver;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

public class Adapter {

    private final AdapterConfig config;
    private final Loader loader;

    public Adapter(AdapterConfig config, Loader loader) {
        this.config = config;
        this.loader = loader;
    }

    /**
     * Retrieve a new connection based on configuration.
     *
     * @return {@link java.sql.Connection}
     */
    public Connection getConnection() {
        try {
            Driver driver = loader.getDriver();
            Properties info = getConnectionProperties();
            Connection connection = driver.connect(config.getConnectionUrl(), info);
            if (Objects.isNull(connection)) {
                throw new ConnectionException("Failed to connect to database", new NullPointerException("connection is null"));
            }
            return connection;
        } catch (LoaderException le) {
            throw new ConnectionException("Failed to load Driver", le);
        } catch (SQLException sqle) {
            throw new ConnectionException("SQLException when connecting", sqle);
        }
    }

    private Properties getConnectionProperties() {
        Properties info = new Properties(config.getConnectionProps());
        if (config.hasUser()) {
            info.setProperty("user", config.getUser());
        }
        if (config.hasPassword()) {
            info.setProperty("password", config.getPassword());
        }
        return info;
    }

}
