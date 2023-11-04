package org.schemaspy.connection;

import java.sql.SQLException;
import org.schemaspy.input.dbms.ConnectionConfig;
import org.schemaspy.input.dbms.ConnectionURLBuilder;
import org.schemaspy.input.dbms.driver.Driversource;

import java.io.IOException;
import java.sql.Driver;
import java.util.Properties;

public final class ScSimple implements SqlConnection{

    private final Connection con;
    private final ConnectionURLBuilder urlBuilder;
    private final Driversource driversource;

    public ScSimple(
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

    public ScSimple(
        final Connection con,
        final ConnectionURLBuilder urlBuilder,
        final Driversource driversource
    ) {
        this.con = con;
        this.urlBuilder = urlBuilder;
        this.driversource = driversource;
    }

    @Override
    public java.sql.Connection connection() throws IOException, SQLException {
        final String connectionURL = urlBuilder.build();
        final Properties connectionProperties = this.con.properties();
        final Driver driver = driversource.driver();
        return driver.connect(connectionURL, connectionProperties);
    }
}
