package org.schemaspy.connection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.schemaspy.input.dbms.ConnectionURLBuilder;
import org.schemaspy.input.dbms.exceptions.ConnectionFailure;

public class ScNullChecked implements SqlConnection {

    private final ConnectionURLBuilder urlBuilder;
    private final SqlConnection origin;

    public ScNullChecked(final ConnectionURLBuilder urlBuilder, final SqlConnection origin) {
        this.urlBuilder = urlBuilder;
        this.origin = origin;
    }

    @Override
    public Connection connection() throws IOException, SQLException {
        Connection result = this.origin.connection();
        if (result == null) {
            throw new ConnectionFailure("Cannot connect to '" + this.urlBuilder.build() + "'");
        }
        return result;
    }
}
