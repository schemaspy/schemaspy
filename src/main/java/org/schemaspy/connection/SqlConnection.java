package org.schemaspy.connection;

import org.schemaspy.input.dbms.ConnectionURLBuilder;
import org.schemaspy.input.dbms.driver.Driversource;
import org.schemaspy.input.dbms.exceptions.ConnectionFailure;

import java.io.IOException;
import java.sql.Driver;
import java.util.Properties;

public final class SqlConnection {

    private final Connection con;
    private final ConnectionURLBuilder urlBuilder;
    private final String[] driverClass;
    private final Driversource driversource;

    public SqlConnection(
        final Connection con,
        final ConnectionURLBuilder urlBuilder,
        final String[] driverClass,
        final Driversource driversource
    ) {
        this.con = con;
        this.urlBuilder = urlBuilder;
        this.driverClass = driverClass;
        this.driversource = driversource;
    }

    public java.sql.Connection connection() throws IOException {
        String connectionURL = urlBuilder.build();
        String[] driverClasses = driverClass;

        final Properties connectionProperties = this.con.properties();

        java.sql.Connection connection;
        try {
            Driver driver = driversource.driver();
            connection = driver.connect(connectionURL, connectionProperties);
            if (connection == null) {
                throw new ConnectionFailure("Cannot connect to '" + connectionURL + "' with driver '" + String.join(",", driverClasses) + "'");
            }
        } catch (UnsatisfiedLinkError badPath) {
            throw new ConnectionFailure("Error with native library occurred while trying to use driver '" + String.join(",", driverClasses) + "'", badPath);
        } catch (Exception exc) {
            throw new ConnectionFailure("Failed to connect to database URL [" + connectionURL + "]", exc);
        }
        return connection;
    }
}
