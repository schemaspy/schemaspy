package org.schemaspy.testing;

import org.schemaspy.LayoutFolder;
import org.schemaspy.SchemaAnalyzer;
import org.schemaspy.cli.*;
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
        return new SchemaSpyRunner(
                new SchemaAnalyzer(
                        sqlService,
                        new DatabaseServiceFactory(sqlService),
                        commandLineArguments,
                        new XmlProducerUsingDOM(),
                        new LayoutFolder(SchemaAnalyzer.class.getClassLoader())
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
