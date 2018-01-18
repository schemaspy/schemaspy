package org.schemaspy.cli;

import org.schemaspy.Config;
import org.schemaspy.input.db.driver.AdapterConfig;
import org.schemaspy.util.ConnectionURLBuilder;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

public class AdapterConfigDelegate implements AdapterConfig {

    private final CommandLineArguments commandLineArguments;
    private final Config config;

    private String connectionUrl;

    public AdapterConfigDelegate(CommandLineArguments commandLineArguments, Config config) {
        this.commandLineArguments = commandLineArguments;
        this.config = config;
    }

    @Override
    public boolean hasUser() {
        return Objects.nonNull(commandLineArguments.getUser()) && !commandLineArguments.getUser().isEmpty();
    }

    @Override
    public String getUser() {
        return commandLineArguments.getUser();
    }

    @Override
    public boolean hasPassword() {
        return Objects.nonNull(config.getPassword()) && !config.getPassword().isEmpty();
    }

    @Override
    public String getPassword() {
        return config.getPassword();
    }

    @Override
    public Properties getConnectionProps() {
        try {
            return config.getConnectionProperties();
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    @Override
    public synchronized String getConnectionUrl() {
        if (Objects.isNull(connectionUrl)) {
            try {
                Properties properties = config.determineDbProperties(commandLineArguments.getDatabaseType());

                ConnectionURLBuilder urlBuilder = new ConnectionURLBuilder(config, properties);
                if (commandLineArguments.getDatabaseName() == null)
                    config.setDb(urlBuilder.build());
                connectionUrl = urlBuilder.build();
            } catch (IOException e) {
                throw new RuntimeIOException(e);
            }
        }
        return connectionUrl;
    }
}
