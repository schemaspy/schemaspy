package org.schemaspy.connection;

import java.sql.Connection;
import org.schemaspy.input.dbms.ConnectionURLBuilder;
import org.schemaspy.input.dbms.exceptions.ConnectionFailure;

public class ScExceptionChecked implements SqlConnection {

    private final ConnectionURLBuilder urlBuilder;
    private final SqlConnection origin;

    public ScExceptionChecked(
        final ConnectionURLBuilder urlBuilder,
        final SqlConnection origin
    ) {
        this.urlBuilder = urlBuilder;
        this.origin = origin;
    }

    @Override
    public Connection connection() {
        try {
            return this.origin.connection();
        } catch (Exception exc) {
            throw new ConnectionFailure(
                "Failed to connect to database URL [" + this.urlBuilder.build() + "]",
                exc
            );
        }
    }
}
