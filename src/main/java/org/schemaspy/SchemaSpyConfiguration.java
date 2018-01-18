package org.schemaspy;

import com.beust.jcommander.ParameterException;
import org.schemaspy.cli.*;
import org.schemaspy.input.db.driver.Adapter;
import org.schemaspy.input.db.driver.Loader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    @Bean
    public CommandLineArguments commandLineArguments(final ApplicationArguments applicationArguments, CommandLineArgumentParser commandLineArgumentParser) {
        Objects.requireNonNull(applicationArguments);
        Objects.requireNonNull(commandLineArgumentParser);

        String[] args = applicationArguments.getSourceArgs();
        Objects.requireNonNull(args);

        CommandLineArguments arguments = parseArgumentsOrExit(commandLineArgumentParser, args);

        return arguments;
    }

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

    private CommandLineArguments parseArgumentsOrExit(CommandLineArgumentParser commandLineArgumentParser, String... args) {
        try {
            return commandLineArgumentParser.parse(args);
        } catch (ParameterException e) {
            LOGGER.error(e.getLocalizedMessage());
            System.exit(1);
            return null;
        }
    }

    @Bean
    public Config config(final ApplicationArguments applicationArguments) {
        return new Config(applicationArguments.getSourceArgs());
    }

    @Bean
    public Adapter adapter(final CommandLineArguments commandLineArguments, final Config config) {
        return new Adapter(new AdapterConfigDelegate(commandLineArguments, config), new Loader(new LoaderConfigDelegate(commandLineArguments, config)));
    }

}
