package org.schemaspy.connection;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

/**
 * Specifies connection properties to use in the format:
 * <code>key1\=value1;key2\=value2</code><br>
 * user (from -u) and password (from -p) will be passed in the
 * connection properties if specified.<p>
 * This is an alternative form of passing connection properties than by file
 */
public class SemicolonSeparated implements Connection {

    private final String properties;

    /**
     * @param properties string with key\\=value pairs separated by ; of connection properties
     */
    public SemicolonSeparated(String properties) {
        this.properties = properties;
    }

    @Override
    public Properties properties() throws IOException {
        Properties result = new Properties();
        result.load(
            new StringReader(
                properties.replace(';','\n')
                    .replace("\\=", "=")
            )
        );
        return result;
    }
}
