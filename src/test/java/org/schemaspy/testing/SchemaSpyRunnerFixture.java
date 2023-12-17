package org.schemaspy.testing;

import org.schemaspy.LayoutFolder;
import org.schemaspy.SchemaAnalyzer;
import org.schemaspy.cli.*;
import org.schemaspy.connection.ScExceptionChecked;
import org.schemaspy.connection.ScNullChecked;
import org.schemaspy.connection.ScSimple;
import org.schemaspy.connection.SqlConnection;
import org.schemaspy.input.dbms.ConnectionConfig;
import org.schemaspy.input.dbms.ConnectionURLBuilder;
import org.schemaspy.input.dbms.DriverFromConfig;
import org.schemaspy.input.dbms.service.DatabaseServiceFactory;
import org.schemaspy.input.dbms.service.SqlService;
import org.schemaspy.output.xml.dom.XmlProducerUsingDOM;

public class SchemaSpyRunnerFixture {
    private SchemaSpyRunnerFixture() {}

    public static SchemaSpyRunner schemaSpyRunner(
            String...args
    ) {
        CommandLineArguments commandLineArguments =
                new CommandLineArgumentParser(
                        new DefaultProviderFactory(
                                new ConfigFileArgumentParser(args).configFile()
                        ).defaultProvider(),
                        args
                ).commandLineArguments();
        SqlService sqlService = new SqlService();
        final ConnectionConfig connectionConfig = commandLineArguments.getConnectionConfig();
        final ConnectionURLBuilder urlBuilder = new ConnectionURLBuilder(connectionConfig);
        return new SchemaSpyRunner(
            new SchemaAnalyzer(
                sqlService,
                new DatabaseServiceFactory(sqlService),
                commandLineArguments,
                new XmlProducerUsingDOM(),
                new LayoutFolder(SchemaAnalyzer.class.getClassLoader()),
                new ScExceptionChecked(
                    urlBuilder,
                    new ScNullChecked(
                        urlBuilder,
                        new ScSimple(
                            connectionConfig,
                            urlBuilder,
                            new DriverFromConfig(connectionConfig)
                        )
                    )
                )
            ),
            commandLineArguments,
            args
        );
    }

    public static SchemaSpyRunner schemaSpyRunner(
            SchemaAnalyzer schemaAnalyzer,
            String...args
    ) {
        CommandLineArgumentParser commandLineArgumentParser =
                new CommandLineArgumentParser(
                        new DefaultProviderFactory(
                                new ConfigFileArgumentParser(args).configFile()
                        ).defaultProvider(),
                        args
                );
        return new SchemaSpyRunner(
                schemaAnalyzer,
                commandLineArgumentParser.commandLineArguments(),
                args
       );
    }
}
