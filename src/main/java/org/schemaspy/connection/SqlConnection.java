package org.schemaspy.connection;

import java.io.IOException;

/**
 * Adapter to the standard library for SQL connections.
 */
public interface SqlConnection {

    /**
     * Asks the connection to interface itself as the standard library.
     * @return The SQL connection as a standard library interface.
     * @throws IOException If the connection is erroneous.
     */
    java.sql.Connection connection() throws IOException;
}
