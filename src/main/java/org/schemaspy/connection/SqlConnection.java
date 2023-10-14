package org.schemaspy.connection;

import org.schemaspy.input.dbms.ConnectionConfig;
import org.schemaspy.input.dbms.ConnectionURLBuilder;
import org.schemaspy.input.dbms.driver.Driversource;
import org.schemaspy.input.dbms.exceptions.ConnectionFailure;

import java.io.IOException;
import java.sql.Driver;
import java.util.Properties;

public final class SqlConnection {

    private final Connection con;
    private final ConnectionURLBuilder urlBuilder;
    private final Driversource driversource;

    public SqlConnection(
        final ConnectionConfig connectionConfig,
        final ConnectionURLBuilder urlBuilder,
        final Driversource driversource
    ) {
        this(
            new WithPassword(
                connectionConfig.getPassword(),
                new WithUser(
                    connectionConfig.getUser(),
                    new PreferencesConnection(connectionConfig.getConnectionProperties())
                )
            ),
            urlBuilder,
            driversource
        );
    }

    public SqlConnection(
        final Connection con,
        final ConnectionURLBuilder urlBuilder,
        final Driversource driversource
    ) {
        this.con = con;
        this.urlBuilder = urlBuilder;
        this.driversource = driversource;
    }

    public java.sql.Connection connection() throws IOException {
        String connectionURL = urlBuilder.build();

        final Properties connectionProperties = this.con.properties();

        java.sql.Connection connection;
        try {
            Driver driver = driversource.driver();
            connection = driver.connect(connectionURL, connectionProperties);
            if (connection == null) {
                throw new ConnectionFailure("Cannot connect to '" + connectionURL + "'");
            }
        } catch (Exception exc) {
            throw new ConnectionFailure("Failed to connect to database URL [" + connectionURL + "]", exc);
        }
        return connection;
    }
}
