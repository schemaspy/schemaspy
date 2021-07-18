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

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.util.Arrays;

import org.schemaspy.Config;
import org.schemaspy.SchemaAnalyzer;
import org.schemaspy.ServiceFixture;
import org.schemaspy.input.dbms.exceptions.ConnectionFailure;
import org.schemaspy.input.dbms.service.DatabaseService;
import org.schemaspy.input.dbms.service.SqlService;
import org.schemaspy.model.EmptySchemaException;
import org.schemaspy.model.InvalidConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.ParameterException;

public class SchemaSpyRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	// ordinal() is automatically unique
	public enum ExitCode {
		OK, GENERIC_ERROR, EMPTY_SCHEMA, CONNECTION_ERROR, CONFIG_ERROR;
	}

	private final CommandLineArguments arguments = new CommandLineArguments();

	private final CommandLineArgumentParser commandLineArgumentParser;

	private final ServiceFixture serviceFixture = new ServiceFixture();

	public SchemaSpyRunner() {
		commandLineArgumentParser = new CommandLineArgumentParser(arguments);
	}

	public ExitCode run(String... args) {
		try {
			commandLineArgumentParser.parse(args);
		} catch (ParameterException e) {
			LOGGER.error(e.getLocalizedMessage(), e);
			return ExitCode.GENERIC_ERROR;
		}
		if (arguments.isHelpRequired()) {
			commandLineArgumentParser.printUsage();
			return ExitCode.OK;
		}

		if (arguments.isDbHelpRequired()) {
			commandLineArgumentParser.printDatabaseTypesHelp();
			return ExitCode.OK;
		}

		if (arguments.isDebug()) {
			enableDebug();
		}

		return runAnalyzer(args);
	}

	public void enableDebug() {
		// TODO: enable debugging
		LOGGER.debug("Debug enabled");
	}

	private ExitCode runAnalyzer(String... args) {
		final SchemaAnalyzer analyzer = createAnalzyer(serviceFixture.getSqlService(),
				serviceFixture.getDatabaseService(), arguments);
		try {
			return analyzer.analyze(new Config(args)) == null ? ExitCode.GENERIC_ERROR : ExitCode.OK;
		} catch (ConnectionFailure couldntConnect) {
			LOGGER.warn("Connection Failure", couldntConnect);
			return ExitCode.CONNECTION_ERROR;
		} catch (EmptySchemaException noData) {
			LOGGER.warn("Empty schema", noData);
			return ExitCode.EMPTY_SCHEMA;
		} catch (InvalidConfigurationException badConfig) {
			LOGGER.debug("Command line parameters: {}", Arrays.asList(args));
			if (badConfig.getParamName() != null) {
				LOGGER.error("Bad parameter: '{} = {}'", badConfig.getParamName(), badConfig.getParamValue(),
						badConfig);
			} else {
				LOGGER.error("Bad config", badConfig);
			}
			return ExitCode.CONFIG_ERROR;
		} catch (SQLException e) {
			LOGGER.error("SqlException", e);
			return ExitCode.GENERIC_ERROR;
		} catch (IOException e) {
			LOGGER.error("IOException", e);
			return ExitCode.GENERIC_ERROR;
		}
	}

	SchemaAnalyzer createAnalzyer(SqlService sqlService, DatabaseService databaseService,
			CommandLineArguments arguments) {
		return new SchemaAnalyzer(sqlService, databaseService, arguments);
	}

}
