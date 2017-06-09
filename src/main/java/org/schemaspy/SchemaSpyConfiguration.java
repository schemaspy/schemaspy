package org.schemaspy;

import com.beust.jcommander.ParameterException;
import org.schemaspy.cli.CommandLineArgumentParser;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.cli.ConfigFileArgumentParser;
import org.schemaspy.cli.PropertyFileDefaultProvider;
import org.schemaspy.cli.PropertyFileDefaultProviderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Configuration
public class SchemaSpyConfiguration {

    private static final Logger LOGGER = Logger.getLogger(SchemaSpyConfiguration.class.getName());

    @Autowired
    private ConfigFileArgumentParser configFileArgumentParser;

    @Autowired
    private PropertyFileDefaultProviderFactory factory;

    @Autowired
    private ApplicationArguments applicationArguments;

    @Bean
    public CommandLineArguments commandLineArguments(ApplicationArguments applicationArguments, CommandLineArgumentParser commandLineArgumentParser) {
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
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
            System.exit(1);
            return null;
        }
    }

}
