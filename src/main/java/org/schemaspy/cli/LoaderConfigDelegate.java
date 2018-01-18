package org.schemaspy.cli;

import org.schemaspy.Config;
import org.schemaspy.input.db.driver.LoaderConfig;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

public class LoaderConfigDelegate implements LoaderConfig {

    private final CommandLineArguments commandLineArguments;
    private final Config config;

    private String driverClass;
    private String driverPath;

    public LoaderConfigDelegate(CommandLineArguments commandLineArguments, Config config) {
        this.commandLineArguments = commandLineArguments;
        this.config = config;
    }

    @Override
    public synchronized String getDriverClass() {
        if (Objects.isNull(driverClass)) {
            populateFields();
        }
        return driverClass;
    }

    @Override
    public String getDriverPath() {
        if (Objects.isNull(driverPath)) {
            populateFields();
        }
        return driverPath;
    }

    private void populateFields() {
        Properties properties = null;
        try {
            properties = config.determineDbProperties(commandLineArguments.getDatabaseType());
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }

        driverClass = properties.getProperty("driver");
        driverPath = properties.getProperty("driverPath");
        if (driverPath == null)
            driverPath = "";
        if (config.getDriverPath() != null)
            driverPath = config.getDriverPath() + File.pathSeparator + driverPath;
    }
}
