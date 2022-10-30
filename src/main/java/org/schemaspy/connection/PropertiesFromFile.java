package org.schemaspy.connection;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Properties from this file (in key=value pair format) are passed to the
 * database connection.<br>
 * user (from -u) and password (from -p) will be passed in the
 * connection properties if specified.
 */
public class PropertiesFromFile implements Connection {

    private final String propertiesFilename;

    /**
     * @param propertiesFilename file to use for connection properties
     */
    public PropertiesFromFile(String propertiesFilename) {
        this.propertiesFilename = propertiesFilename;
    }

    @Override
    public Properties properties() throws IOException {
        Properties result = new Properties();
        try (InputStream inputStream = new FileInputStream(propertiesFilename)) {
            result.load(inputStream);
        }
        return result;
    }
}
