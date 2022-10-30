package org.schemaspy.connection;

import java.io.IOException;
import java.util.Properties;

/**
 * Abstracts a connection to a database
 */
public interface Connection {

    /**
     * Asks the connection to provide details about its properties.
     * @return The connection properties.
     * @throws IOException If the connection is erroneous.
     */
    Properties properties() throws IOException;
}
