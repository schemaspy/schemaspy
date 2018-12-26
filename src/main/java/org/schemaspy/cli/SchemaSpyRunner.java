/*
 * Copyright (C) 2018 Nils Petzaell
 *
 * This file is part of SchemaSpy.
 *
 * SchemaSpy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SchemaSpy. If not, see <http://www.gnu.org/licenses/>.
 */
package org.schemaspy.cli;

import com.beust.jcommander.ParameterException;
import org.schemaspy.Config;
import org.schemaspy.SchemaAnalyzer;
import org.schemaspy.input.dbms.exceptions.ConnectionFailure;
import org.schemaspy.model.EmptySchemaException;
import org.schemaspy.model.InvalidConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.util.Arrays;

@Component
public class SchemaSpyRunner implements ExitCodeGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final int EXIT_CODE_OK = 0;
    private static final int EXIT_CODE_GENERIC_ERROR = 1;
    private static final int EXIT_CODE_EMPTY_SCHEMA = 2;
    private static final int EXIT_CODE_CONNECTION_ERROR = 3;
    private static final int EXIT_CODE_CONFIG_ERROR = 4;

    @Autowired
    private SchemaAnalyzer analyzer;

    @Autowired
    private CommandLineArguments arguments;

    @Autowired
    private CommandLineArgumentParser commandLineArgumentParser;

    @Autowired
    private LoggingSystem loggingSystem;

    private int exitCode = EXIT_CODE_OK;

    public void run(String... args) {
        try {
            commandLineArgumentParser.parse(args);
        } catch (ParameterException e) {
            LOGGER.error(e.getLocalizedMessage(),e);
            exitCode = 1;
            return;
        }
        if (arguments.isHelpRequired()) {
            commandLineArgumentParser.printUsage();
            return;
        }

        if (arguments.isDbHelpRequired()) {
            commandLineArgumentParser.printDatabaseTypesHelp();
            return;
        }

        if (arguments.isDebug()) {
            enableDebug();
        }

        runAnalyzer(args);
    }

    public void enableDebug() {
        loggingSystem.setLogLevel("org.schemaspy", LogLevel.DEBUG);
        LOGGER.debug("Debug enabled");
    }

    private void runAnalyzer(String... args) {
        exitCode = EXIT_CODE_GENERIC_ERROR;
        try {
            exitCode = analyzer.analyze(new Config(args)) == null ? EXIT_CODE_GENERIC_ERROR : EXIT_CODE_OK;
        } catch (ConnectionFailure couldntConnect) {
            LOGGER.warn("Connection Failure", couldntConnect);
            exitCode = EXIT_CODE_CONNECTION_ERROR;
        } catch (EmptySchemaException noData) {
            LOGGER.warn("Empty schema", noData);
            exitCode = EXIT_CODE_EMPTY_SCHEMA;
        } catch (InvalidConfigurationException badConfig) {
            exitCode = EXIT_CODE_CONFIG_ERROR;
            LOGGER.debug("Command line parameters: {}", Arrays.asList(args));
            if (badConfig.getParamName() != null) {
                LOGGER.error("Bad parameter: '{} = {}'", badConfig.getParamName(), badConfig.getParamValue(), badConfig);
            } else {
                LOGGER.error("Bad config", badConfig);
            }
        } catch (SQLException e) {
            LOGGER.error("SqlException", e);
        } catch (IOException e) {
            LOGGER.error("IOException", e);
        }
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
}
