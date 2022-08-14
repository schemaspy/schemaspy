package org.schemaspy.connection;

import org.schemaspy.Config;

import java.io.IOException;
import java.util.Properties;

public class ConnectionPropertiesFactory {
    private final String connprops;
    private Properties userConnectionProperties;
    private static final String ESCAPED_EQUALS = "\\=";

    public ConnectionPropertiesFactory(Config config) {
        this(config.connprops());
    }

    public ConnectionPropertiesFactory(String connprops) {
        this.connprops = connprops;
    }

    /**
     * Returns a {@link Properties} populated either from the properties file specified
     * by {@link PropertiesFromFile}, the properties specified by
     * {@link SemicolonSeparated} or not populated.
     *
     * @return connection properties to use when connecting
     * @throws IOException if we a have problems reading the properties file if -connprops is a file
     */
    public Properties getConnectionProperties() throws IOException {
        if (userConnectionProperties == null) {
            if (connprops != null) {
                if (connprops.contains(ESCAPED_EQUALS)) {
                    userConnectionProperties = new SemicolonSeparated(connprops).setConnectionProperties();
                } else {
                    userConnectionProperties = new PropertiesFromFile(connprops).setConnectionPropertiesFile();
                }
            } else {
                userConnectionProperties = new Properties();
            }
        }

        return userConnectionProperties;
    }
}
