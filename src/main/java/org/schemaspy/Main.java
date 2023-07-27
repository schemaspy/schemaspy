/*
 * Copyright (C) 2004 - 2011 John Currier
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2017 Wojciech Kasa
 * Copyright (C) 2017 Thomas Traude
 * Copyright (C) 2017 Ismail Simsek
 * Copyright (C) 2017 Daniel Watt
 * Copyright (C) 2018 Nils Petzaell
 *
 * This file is a part of the SchemaSpy project (http://schemaspy.org).
 *
 * SchemaSpy is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.schemaspy;

import com.beust.jcommander.ParameterException;
import org.schemaspy.cli.*;
import org.schemaspy.input.dbms.service.DatabaseServiceFactory;
import org.schemaspy.input.dbms.service.SqlService;
import org.schemaspy.logging.StackTraceOmitter;
import org.schemaspy.output.xml.dom.XmlProducerUsingDOM;
import org.schemaspy.util.ManifestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Objects;

/**
 * @author John Currier
 * @author Rafal Kasa
 * @author Wojciech Kasa
 * @author Thomas Traude
 * @author Ismail Simsek
 * @author Daniel Watt
 * @author Nils Petzaell
 */
@SpringBootApplication
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String... args) {
        System.out.println(new Banner( //NOSONAR
                "/banner.txt",
                Map.of("${application.version}", ManifestUtils.getImplementationVersion())
                ).banner()
        );
        ConfigurableApplicationContext context = SpringApplication.run(Main.class, args);
        CommandLineArgumentParser commandLineArgumentParser =
                new CommandLineArgumentParser(
                        new DefaultProviderFactory(
                                new ConfigFileArgumentParser(args).configFile()
                        ).defaultProvider(),
                        args
                );
        CommandLineArguments arguments = parse(commandLineArgumentParser);
        int exitCode = Objects.nonNull(arguments)
                ? run(commandLineArgumentParser, arguments, context.getBean(LoggingSystem.class), args)
                : 1;
        if (StackTraceOmitter.hasOmittedStackTrace()) {
            LOGGER.info("StackTraces have been omitted, use `-debug` when executing SchemaSpy to see them");
        }
        System.exit(exitCode);
    }

    private static CommandLineArguments parse(CommandLineArgumentParser commandLineArgumentParser) {
        try {
            return commandLineArgumentParser.commandLineArguments();
        } catch (ParameterException e) {
            LOGGER.error("Invalid command line arguments:",e);
            return null;
        }
    }

    private static int run(
            CommandLineArgumentParser commandLineArgumentParser,
            CommandLineArguments arguments,
            LoggingSystem loggingSystem,
            String...args
    ) {
        if (arguments.isHelpRequired()) {
            commandLineArgumentParser.printUsage();
            return 0;
        }

        if (arguments.isDbHelpRequired()) {
            commandLineArgumentParser.printDatabaseTypesHelp();
            return 0;
        }

        if (arguments.isDebug()) {
            loggingSystem.setLogLevel("org.schemaspy", LogLevel.DEBUG);
            LOGGER.debug("Debug enabled");
        }
        SqlService sqlService = new SqlService();
        SchemaSpyRunner schemaSpyRunner =
                new SchemaSpyRunner(
                        new SchemaAnalyzer(
                                sqlService,
                                new DatabaseServiceFactory(sqlService),
                                arguments,
                                new XmlProducerUsingDOM(),
                                new LayoutFolder(SchemaAnalyzer.class.getClassLoader())
                        ),
                        arguments,
                        args
                );
        schemaSpyRunner.run();
        return schemaSpyRunner.getExitCode();
    }

}