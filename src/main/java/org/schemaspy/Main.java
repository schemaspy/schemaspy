/*
 * This file is a part of the SchemaSpy project (http://schemaspy.org).
 * Copyright (C) 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011 John Currier
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

import org.schemaspy.cli.CommandLineArgumentParser;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.model.ConnectionFailure;
import org.schemaspy.model.EmptySchemaException;
import org.schemaspy.model.InvalidConfigurationException;
import org.schemaspy.model.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;

@SpringBootApplication
public class Main implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private SchemaAnalyzer analyzer;

    @Autowired
    private CommandLineArguments arguments;

    @Autowired
    private CommandLineArgumentParser commandLineArgumentParser;

    @Autowired
    private ApplicationContext context;

    public static void main(String... args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) {
        if (arguments.isHelpRequired()) {
            commandLineArgumentParser.printUsage();
            exitApplication(0);
            return;
        }

        if (arguments.isDbHelpRequired()) {
            commandLineArgumentParser.printDatabaseTypesHelp();
            exitApplication(0);
            return;
        }

        runAnalyzer(args);
    }

    private void runAnalyzer(String... args) {
        int rc = 1;

        try {
            rc = analyzer.analyze(new Config(args)) == null ? 1 : 0;
        } catch (ConnectionFailure couldntConnect) {
            LOGGER.warn("Connection Failure", couldntConnect);
            rc = 3;
        } catch (EmptySchemaException noData) {
            LOGGER.warn("Empty schema", noData);
            rc = 2;
        } catch (InvalidConfigurationException badConfig) {
            LOGGER.debug("Command line parameters: {}", Arrays.asList(args));
            if (badConfig.getParamName() != null) {
                LOGGER.error("Bad parameter '{} {}' , {}", badConfig.getParamName(), badConfig.getParamValue(), badConfig.getMessage(), badConfig);
            } else {
                LOGGER.error("Bad config {}", badConfig.getMessage(), badConfig);
            }
        } catch (ProcessExecutionException badLaunch) {
            LOGGER.warn(badLaunch.getMessage(), badLaunch);
        } catch (Exception exc) {
            LOGGER.error(exc.getMessage(), exc);
        }

        exitApplication(rc);
    }

    private void exitApplication(int returnCode) {
        SpringApplication.exit(context, () -> returnCode);
    }

}