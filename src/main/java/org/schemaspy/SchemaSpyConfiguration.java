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
