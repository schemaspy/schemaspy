package org.schemaspy.testing;

import org.schemaspy.cli.CommandLineArgumentParser;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.connection.ScSimple;
import org.schemaspy.input.dbms.ConnectionConfig;
import org.schemaspy.input.dbms.ConnectionURLBuilder;
import org.schemaspy.input.dbms.DriverFromConfig;
import org.schemaspy.input.dbms.service.DatabaseServiceFactory;
import org.schemaspy.input.dbms.service.SqlService;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;

import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Optional;

import static org.mockito.Mockito.mock;

public class DatabaseFixture {
    private DatabaseFixture() { }

    public static Database database(String...args) throws SQLException, IOException {
        return database(mock(ProgressListener.class), args);
    }

    public static Database database(
            ProgressListener progressListener,
            String...args
    ) throws SQLException, IOException {
        SqlService sqlService = new SqlService();
        CommandLineArguments arguments = new CommandLineArgumentParser(
                args
        ).commandLineArguments();
        ConnectionConfig connectionConfig = arguments.getConnectionConfig();
        DatabaseMetaData metaData = sqlService.connect(
            new ScSimple(
                connectionConfig,
                new ConnectionURLBuilder(connectionConfig),
                new DriverFromConfig(connectionConfig)
            )
        );
        Database database = new Database(
                sqlService.getDbmsMeta(),
                arguments.getConnectionConfig().getDatabaseName(),
                Optional.ofNullable(arguments.getCatalog()).orElse(metaData.getConnection().getCatalog()),
                Optional.ofNullable(arguments.getSchema()).orElse(metaData.getConnection().getSchema())
        );
        new DatabaseServiceFactory(sqlService)
                .forSingleSchema(arguments.getProcessingConfig())
                .gatherSchemaDetails(database, null, progressListener);
        return database;
    }

}
