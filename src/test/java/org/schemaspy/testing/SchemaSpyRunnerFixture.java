package org.schemaspy.testing;

import org.schemaspy.LayoutFolder;
import org.schemaspy.SchemaAnalyzer;
import org.schemaspy.cli.CommandLineArgumentParser;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.cli.SchemaSpyRunner;
import org.schemaspy.input.dbms.service.DatabaseServiceFactory;
import org.schemaspy.input.dbms.service.SqlService;
import org.schemaspy.output.xml.dom.XmlProducerUsingDOM;

public class SchemaSpyRunnerFixture {
    private SchemaSpyRunnerFixture() {}

    public static SchemaSpyRunner schemaSpyRunner(
            CommandLineArgumentParser commandLineArgumentParser,
            String...args
    ) {
        CommandLineArguments commandLineArguments = commandLineArgumentParser.parse(args);
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
            CommandLineArgumentParser commandLineArgumentParser,
            String...args
    ) {
        CommandLineArguments commandLineArguments = commandLineArgumentParser.parse(args);
        return new SchemaSpyRunner(
                schemaAnalyzer,
                commandLineArguments,
                args
       );
    }
}
