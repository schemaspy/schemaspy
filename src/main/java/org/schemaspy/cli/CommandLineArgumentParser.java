/*
 * Copyright (C) 2017 Thomas Traude
 * Copyright (C) 2017 Daniel Watt
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.schemaspy.Config;
import org.schemaspy.input.dbms.config.PropertiesResolver;
import org.schemaspy.util.DbSpecificConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.IDefaultProvider;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterDescription;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.internal.Console;

/**
 * This class uses {@link JCommander} to parse the SchemaSpy command line
 * arguments represented by {@link CommandLineArguments}.
 *
 * @author Thomas Traude
 * @author Daniel Watt
 * @author Nils Petzaell
 */
public class CommandLineArgumentParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final PropertiesResolver propertiesResolver = new PropertiesResolver();

	private JCommander jCommander;

	private final CommandLineArguments arguments;

	private final Function<String[], IDefaultProvider> defaultProviderFactory;

	private static final String[] requiredFields = { "outputDirectory" };

	public CommandLineArgumentParser(CommandLineArguments commandLineArguments) {
		this(commandLineArguments, CommandLineArgumentParser::createDefaultProvider);
	}

	CommandLineArgumentParser(CommandLineArguments commandLineArguments,
			Function<String[], IDefaultProvider> defaultProviderFactory) {
		this.arguments = commandLineArguments;
		this.defaultProviderFactory = defaultProviderFactory;
	}

	public CommandLineArguments parse(String... localArgs) {
		final IDefaultProvider dp = defaultProviderFactory.apply(localArgs);
		Locale.setDefault(Locale.ENGLISH);
		jCommander = JCommander.newBuilder().acceptUnknownOptions(true)
				.programName("java -jar " + Config.getLoadedFromJar()).columnSize(120).defaultProvider(dp).build();
		jCommander.addObject(arguments);
		jCommander.parse(localArgs);

		if (shouldValidate()) {
			validate(arguments);
		}
		return arguments;
	}

	private static IDefaultProvider createDefaultProvider(String... localArgs) {
		final ConfigFileArgumentParser configFileArgumentParser = new ConfigFileArgumentParser();
		final DefaultProviderFactory defaultProviderFactory = new DefaultProviderFactory();
		return defaultProviderFactory
				.create(configFileArgumentParser.parseConfigFileArgumentValue(localArgs).orElse(null));
	}

	private boolean shouldValidate() {
		List<ParameterDescription> helpParameters = jCommander.getParameters().stream()
				.filter(ParameterDescription::isHelp).collect(Collectors.toList());
		for (ParameterDescription parameterDescription : helpParameters) {
			if (parameterDescription.isAssigned()) {
				return false;
			}
		}
		return true;
	}

	private void validate(CommandLineArguments arguments) {
		List<String> runtimeRequiredFields = computeRequiredFields(arguments);

		List<String> missingFields = new ArrayList<>();
		Map<String, ParameterDescription> fieldToParameterDescription = jCommander.getParameters().stream()
				.collect(Collectors.toMap(parameterDescription -> parameterDescription.getParameterized().getName(),
						parameterDescription -> parameterDescription));
		for (String field : runtimeRequiredFields) {
			ParameterDescription parameterDescription = fieldToParameterDescription.get(field);
			if (valueIsMissing(parameterDescription)) {
				missingFields.add("[" + String.join(" | ", parameterDescription.getParameter().names()) + "]");
			}
		}
		if (!missingFields.isEmpty()) {
			String message = String.join(", ", missingFields);
			throw new ParameterException("The following "
					+ (missingFields.size() == 1 ? "option is required: " : "options are required: ") + message);
		}
	}

	private static List<String> computeRequiredFields(CommandLineArguments arguments) {
		List<String> computedRequiredFields = new ArrayList<>(Arrays.asList(requiredFields));
		if (!arguments.isSingleSignOn()) {
			computedRequiredFields.add("user");
		}
		return computedRequiredFields;
	}

	private static boolean valueIsMissing(ParameterDescription parameterDescription) {
		Object value = parameterDescription.getParameterized().get(parameterDescription.getObject());
		if (value instanceof String) {
			return ((String) value).isEmpty();
		}
		return Objects.isNull(value);
	}

	/**
	 * Prints documentation about the usage of command line arguments to the
	 * console.
	 * <p>
	 */
	// TODO consider extracting dump generation to other class
	public void printUsage() {

		final Console console = new Console() {
			@Override
			public void print(String msg) {
				LOGGER.info(msg);
			}

			@Override
			public void println(String msg) {
				LOGGER.info(msg + "\n");
			}

			@Override
			public char[] readPassword(boolean echoInput) {
				throw new UnsupportedOperationException();
			}
		};
		jCommander.setConsole(console);
		jCommander.usage();

		console.println("Go to http://schemaspy.org for a complete list/description of additional parameters.\n");
		console.println("Sample usage using the default database type (implied -t ora):\n");
		console.println(" java -jar schemaSpy.jar -db mydb -s myschema -u devuser -p password -o output\n");
	}

	/**
	 * Prints information of supported database types to the console.
	 * <p>
	 */
	public void printDatabaseTypesHelp() {
		String schemaspyJarFileName = Config.getLoadedFromJar();

		LOGGER.info("Built-in database types and their required connection parameters:");
		for (String type : Config.getBuiltInDatabaseTypes(schemaspyJarFileName)) {
			new DbSpecificConfig(type, propertiesResolver.getDbProperties(type)).dumpUsage();
		}
		LOGGER.info("You can use your own database types by specifying the filespec of a .properties file with -t.");
		LOGGER.info("Grab one out of {} and modify it to suit your needs.", schemaspyJarFileName);
	}

	public void printLicense() {
		final ClassLoader cl = ClassLoader.getSystemClassLoader();
		try (BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(cl.getResourceAsStream("COPYING"), StandardCharsets.UTF_8))) {
			bufferedReader.lines().forEachOrdered(LOGGER::info);
		} catch (IOException e) {
			LOGGER.error("Failed to read COPYING (GPL)", e);
		}
		try (BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(cl.getResourceAsStream("COPYING.LESSER"), StandardCharsets.UTF_8))) {
			bufferedReader.lines().forEachOrdered(LOGGER::info);
		} catch (IOException e) {
			LOGGER.error("Failed to read COPYING.LESSER (LGPL)", e);
		}
	}
}
