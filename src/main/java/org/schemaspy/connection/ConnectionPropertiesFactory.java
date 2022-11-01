package org.schemaspy.connection;

import org.schemaspy.Config;

import java.io.IOException;
import java.util.Properties;

public class ConnectionPropertiesFactory {
    private final String connprops;
    private Connection userConnection;
    private static final String ESCAPED_EQUALS = "\\=";

    public ConnectionPropertiesFactory(Config config) {
        this(config.connprops());
    }

    public ConnectionPropertiesFactory(String connprops) {
        this.connprops = connprops;
    }

    /**
     * Returns a {@link Connection} populated either from the properties file specified
     * by {@link PropertiesFromFile}, the properties specified by
     * {@link SemicolonSeparated} or not populated.
     *
     * @return connection properties to use when connecting
     * @throws IOException if we a have problems reading the properties file if -connprops is a file
     */
    public Connection getConnectionProperties() throws IOException {
        if (userConnection == null) {
            if (connprops != null) {
                if (connprops.contains(ESCAPED_EQUALS)) {
                    userConnection = new SemicolonSeparated(connprops);
                } else {
                    userConnection = new PropertiesFromFile(connprops);
                }
            } else {
                userConnection = Properties::new;
            }
        }

        return userConnection;
    }
}
