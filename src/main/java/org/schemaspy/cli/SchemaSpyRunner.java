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
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.schemaspy.Config;
import org.schemaspy.ContextFixture;
import org.schemaspy.SchemaAnalyzer;
import org.schemaspy.input.dbms.exceptions.ConnectionFailure;
import org.schemaspy.input.dbms.service.DatabaseServiceFactory;
import org.schemaspy.model.EmptySchemaException;
import org.schemaspy.model.InvalidConfigurationException;
import org.schemaspy.output.xml.dom.XmlProducerUsingDOM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ExitCodeGenerator;

import com.beust.jcommander.IDefaultProvider;
import com.beust.jcommander.ParameterException;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

public class SchemaSpyRunner implements ExitCodeGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final int EXIT_CODE_OK = 0;
	private static final int EXIT_CODE_GENERIC_ERROR = 1;
	private static final int EXIT_CODE_EMPTY_SCHEMA = 2;
	private static final int EXIT_CODE_CONNECTION_ERROR = 3;
	private static final int EXIT_CODE_CONFIG_ERROR = 4;

	private final ContextFixture fixture = new ContextFixture();

	private int exitCode = EXIT_CODE_OK;

	private final Function<ContextFixture, SchemaAnalyzer> analyzerFactory;

	public SchemaSpyRunner() {
		this(ctx -> new SchemaAnalyzer(ctx.sqlService(), new DatabaseServiceFactory(ctx.sqlService()),
				ctx.commandLineArguments(), new XmlProducerUsingDOM()));
	}

	public SchemaSpyRunner(Function<ContextFixture, SchemaAnalyzer> analyzerFactory) {
		this.analyzerFactory = analyzerFactory;
	}

	public void run(String... args) {
		Optional<String> configFileName = fixture.configFileArgumentParser().parseConfigFileArgumentValue(args);
		final IDefaultProvider iDefaultProvider = fixture.defaultProviderFactory().create(configFileName.orElse(null));
		final CommandLineArgumentParser commandLineArgumentParser = new CommandLineArgumentParser(
				fixture.commandLineArguments(), iDefaultProvider);

		try {
			commandLineArgumentParser.parse(args);
		} catch (ParameterException e) {
			LOGGER.error(e.getLocalizedMessage(), e);
			exitCode = 1;
			return;
		}
		final CommandLineArguments arguments = fixture.commandLineArguments();
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
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		List<ch.qos.logback.classic.Logger> loggerList = loggerContext.getLoggerList();
		loggerList.stream().filter(logger -> "org.schemaspy".equals(logger.getName())).findFirst()
				.ifPresent(logger -> logger.setLevel(Level.DEBUG));

		LOGGER.debug("Debug enabled");
	}

	private void runAnalyzer(String... args) {

		final SchemaAnalyzer analyzer = analyzerFactory.apply(fixture);

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
				LOGGER.error("Bad parameter: '{} = {}'", badConfig.getParamName(), badConfig.getParamValue(),
						badConfig);
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
