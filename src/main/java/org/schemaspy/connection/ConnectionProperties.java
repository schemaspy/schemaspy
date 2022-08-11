package org.schemaspy.connection;

import org.schemaspy.Config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConnectionProperties {
    private final String connprops;
    private Properties userConnectionProperties;
    private static final String ESCAPED_EQUALS = "\\=";

    public ConnectionProperties(Config config) {
        this(config.connprops());
    }

    public ConnectionProperties(String connprops) {
        this.connprops = connprops;
    }

    /**
     * Returns a {@link Properties} populated either from the properties file specified
     * by {@link #setConnectionPropertiesFile(String)}, the properties specified by
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
                    setConnectionPropertiesFile(connprops);
                }
            } else {
                userConnectionProperties = new Properties();
            }
        }

        return userConnectionProperties;
    }

    /**
     * Properties from this file (in key=value pair format) are passed to the
     * database connection.<br>
     * user (from -u) and password (from -p) will be passed in the
     * connection properties if specified.
     *
     * @param propertiesFilename file to use for connection properties
     * @throws IOException if we have problems reading the file
     */
    private void setConnectionPropertiesFile(String propertiesFilename) throws IOException {
        if (userConnectionProperties == null)
            userConnectionProperties = new Properties();
        try (InputStream inputStream = new FileInputStream(propertiesFilename)) {
            userConnectionProperties.load(inputStream);
        }
    }
}
