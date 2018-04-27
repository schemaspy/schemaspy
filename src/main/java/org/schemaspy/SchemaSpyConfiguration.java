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
package org.schemaspy;

import com.beust.jcommander.ParameterException;
import org.schemaspy.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Thomas Traude
 * @author Daniel Watt
 * @author Nils Petzaell
 */
@Configuration
public class SchemaSpyConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private ConfigFileArgumentParser configFileArgumentParser;

    @Autowired
    private PropertyFileDefaultProviderFactory factory;

    @Autowired
    private LoggingSystem loggingSystem;

    @Bean
    public CommandLineArgumentParser commandLineArgumentParser(ApplicationArguments applicationArguments) {
        Objects.requireNonNull(applicationArguments);

        String[] args = applicationArguments.getSourceArgs();

        Objects.requireNonNull(args);
        Optional<PropertyFileDefaultProvider> propertyFileDefaultProvider = findDefaultProvider(args);
        return new CommandLineArgumentParser(propertyFileDefaultProvider.orElse(null));
    }

    private Optional<PropertyFileDefaultProvider> findDefaultProvider(String... args) {
        Optional<String> configFileName = configFileArgumentParser.parseConfigFileArgumentValue(args);
        return factory.create(configFileName.orElse(null));
    }

    @Bean
    public CommandLineArguments commandLineArguments(ApplicationArguments applicationArguments, CommandLineArgumentParser commandLineArgumentParser, ConfigurableEnvironment environment) {
        Objects.requireNonNull(applicationArguments);
        Objects.requireNonNull(commandLineArgumentParser);

        String[] args = applicationArguments.getSourceArgs();
        Objects.requireNonNull(args);

        CommandLineArguments arguments = parseArgumentsOrExit(commandLineArgumentParser, args);
        if (arguments.isDebug()) {
            enableDebug();
        }
        return arguments;
    }

    private CommandLineArguments parseArgumentsOrExit(CommandLineArgumentParser commandLineArgumentParser, String... args) {
        try {
            return commandLineArgumentParser.parse(args);
        } catch (ParameterException e) {
            LOGGER.error(e.getLocalizedMessage());
            System.exit(1);
            return null;
        }
    }

    public void enableDebug() {
        loggingSystem.setLogLevel("org.schemaspy", LogLevel.DEBUG);
        LOGGER.debug("Debug enabled");
    }

}
