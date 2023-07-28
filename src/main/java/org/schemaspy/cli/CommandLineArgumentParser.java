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

import com.beust.jcommander.IDefaultProvider;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterDescription;
import com.beust.jcommander.ParameterException;
import org.schemaspy.input.dbms.DatabaseTypes;
import org.schemaspy.input.dbms.config.PropertiesResolver;
import org.schemaspy.input.dbms.config.SimplePropertiesResolver;
import org.schemaspy.util.DbSpecificConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class uses {@link JCommander} to parse the SchemaSpy command line arguments represented by {@link CommandLineArguments}.
 *
 * @author Thomas Traude
 * @author Daniel Watt
 * @author Nils Petzaell
 */
public class CommandLineArgumentParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final PropertiesResolver propertiesResolver = new SimplePropertiesResolver();
    private static final String[] requiredFields = {"outputDirectory"};

    private final CommandLineArguments arguments = new CommandLineArguments();

    private final String[] args;
    private final JCommander jCommander;

    public CommandLineArgumentParser(String...args) {
        this(null, args);
    }

    public CommandLineArgumentParser(IDefaultProvider defaultProvider, String...args) {
        this.args = args;
        jCommander = JCommander.newBuilder()
                .acceptUnknownOptions(true)
                .programName("java -jar \"" + Paths.get("").toAbsolutePath().relativize(new SchemaSpyJarFile().path()) + "\"")
                .columnSize(120)
                .defaultProvider(defaultProvider)
                .build();
        jCommander.addObject(new ConfigFileArgument());
        jCommander.addObject(arguments);
    }

    public CommandLineArguments commandLineArguments() {
        jCommander.parse(args);
        arguments.setUnknownArgs(jCommander.getUnknownOptions());
        if (shouldValidate()) {
            validate();
        }
        return arguments;
    }

    private boolean shouldValidate() {
        List<ParameterDescription> helpParameters = jCommander.getParameters()
                .stream()
                .filter(ParameterDescription::isHelp)
                .toList();
        for(ParameterDescription parameterDescription: helpParameters) {
            if (parameterDescription.isAssigned()) {
                return false;
            }
        }
        return true;
    }

    private void validate() {
        List<String> runtimeRequiredFields = computeRequiredFields();

        List<String> missingFields = new ArrayList<>();
        Map<String, ParameterDescription> fieldToParameterDescription = jCommander.getParameters()
                .stream().collect(Collectors.toMap(
                        parameterDescription -> parameterDescription.getParameterized().getName(),
                        parameterDescription -> parameterDescription ));
        for (String field : runtimeRequiredFields) {
            ParameterDescription parameterDescription = Objects.requireNonNull(
                    fieldToParameterDescription.get(field),
                    String.format("%s is declared required, but there is no ParameterDescription", field)
            );
            if (valueIsMissing(parameterDescription)) {
                missingFields.add("[" + String.join(" | ", parameterDescription.getParameter().names()) + "]");
            }
        }
        if (!missingFields.isEmpty()) {
            String message = String.join(", ", missingFields);
            throw new ParameterException("The following "
                    + (missingFields.size() == 1 ? "option is required: " : "options are required: ")
                    + message);
        }
    }

    private List<String> computeRequiredFields() {
        List<String> computedRequiredFields = new ArrayList<>(Arrays.asList(requiredFields));
        if (!arguments.isSingleSignOn()) {
            computedRequiredFields.add("user");
        }
        return computedRequiredFields;
    }

    private static boolean valueIsMissing(ParameterDescription parameterDescription) {
        Object value = parameterDescription.getParameterized().get(parameterDescription.getObject());
        if (value instanceof String) {
            return ((String)value).isEmpty();
        }
        return Objects.isNull(value);
    }


    /**
     * Prints documentation about the usage of command line arguments to the console.
     */
    public void printUsage() {
        StringBuilder builder = new StringBuilder();

        jCommander.usage(builder);

        builder.append(System.lineSeparator());
        builder.append("Go to http://schemaspy.org for a complete list/description of additional parameters.");
        builder.append(System.lineSeparator());
        builder.append(System.lineSeparator());
        builder.append("Sample usage using the default database type (implied -t ora):");
        builder.append(System.lineSeparator());
        builder.append(System.lineSeparator());
        builder.append(" java -jar schemaSpy.jar -db mydb -s myschema -u devuser -p password -o output");
        builder.append(System.lineSeparator());
        builder.append(System.lineSeparator());

        LOGGER.info("{}", builder);
    }

    /**
     * Prints information of supported database types to the console.
     * <p>
     */
    public void printDatabaseTypesHelp() {
        LOGGER.info("Built-in database types and their required connection parameters:");
        TreeMap<String, List<DbSpecificConfig>> builtIns = new TreeMap<>();
        for (String type : new DatabaseTypes().getBuiltInDatabaseTypes()) {
            Properties props = propertiesResolver.getDbProperties(type);
            String dbms = props.getProperty("dbms");
            builtIns.putIfAbsent(dbms, new ArrayList<>());
            builtIns.get(dbms).add(new DbSpecificConfig(type, props));
        }
        builtIns.forEach((key, types) -> {
            LOGGER.info(key);
            types.forEach(dbSpecificConfig -> dbSpecificConfig.dumpUsage(LOGGER));
        });
        LOGGER.info("You can use your own database types by specifying the filespec of a .properties file with -t.");
        LOGGER.info("Grab one out of {} and modify it to suit your needs.", new SchemaSpyJarFile().path());
    }

    public void printLicense() {
        Resource gpl = new ClassPathResource("COPYING");
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(gpl.getInputStream(), StandardCharsets.UTF_8))) {
            bufferedReader.lines().forEachOrdered(LOGGER::info);
        } catch (IOException e) {
            LOGGER.error("Failed to read COPYING (GPL)", e);
        }
        Resource lgpl = new ClassPathResource("COPYING.LESSER");
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(lgpl.getInputStream(), StandardCharsets.UTF_8))) {
            bufferedReader.lines().forEachOrdered(LOGGER::info);
        } catch (IOException e) {
            LOGGER.error("Failed to read COPYING.LESSER (LGPL)", e);
        }
    }
}
