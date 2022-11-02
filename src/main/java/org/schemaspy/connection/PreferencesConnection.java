package org.schemaspy.connection;

import org.schemaspy.Config;

import java.io.IOException;
import java.util.Properties;

/**
 * Encapsulates what connection to use based on user preferences.
 */
public class PreferencesConnection implements Connection {
    private final String connprops;
    private Connection origin;
    private static final String ESCAPED_EQUALS = "\\=";

    public PreferencesConnection(Config config) {
        this(config.connprops());
    }

    public PreferencesConnection(String connprops) {
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
    @Override
    public Properties properties() throws IOException {
        if (origin == null) {
            if (connprops != null) {
                if (connprops.contains(ESCAPED_EQUALS)) {
                    origin = new SemicolonSeparated(connprops);
                } else {
                    origin = new PropertiesFromFile(connprops);
                }
            } else {
                origin = Properties::new;
            }
        }

        return origin.properties();
    }
}
