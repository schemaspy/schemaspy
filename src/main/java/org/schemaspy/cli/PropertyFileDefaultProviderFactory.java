package org.schemaspy.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.Optional;

/**
 * This class creates instances of {@link PropertyFileDefaultProvider} based on a name of the {@link java.util.Properties} file.
 */
@Component
public class PropertyFileDefaultProviderFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String DEFAULT_PROPERTIES_FILE_NAME = "schemaspy.properties";

    private static boolean exists(String propertiesFilename) {
        return new File(propertiesFilename).exists();
    }

    /**
     * Return a {@link PropertyFileDefaultProvider} instance based on propertiesFilename.
     * <p>
     * If for the given propertiesFilename there exists no file the method will exit the application.
     * <p>
     * If the given propertiesFilename is null it falls back to the {@link #DEFAULT_PROPERTIES_FILE_NAME}.
     * If this file does not exist the method returns {@link Optional#empty()}.
     *
     * @param propertiesFilename
     * @return PropertyFileDefaultProvider instance of the propertiesfileName, empty if no such file exists.
     */
    public Optional<PropertyFileDefaultProvider> create(String propertiesFilename) {
        if (propertiesFilename != null) {
            if (exists(propertiesFilename)) {
				LOGGER.info("Found configuration file: {}", propertiesFilename);
                PropertyFileDefaultProvider value = new PropertyFileDefaultProvider(propertiesFilename);
                return Optional.of(value);
            } else {
				LOGGER.error("Could not find config file: {}", propertiesFilename);
                System.exit(0);
                return null;
            }
        }

        if (exists(DEFAULT_PROPERTIES_FILE_NAME)) {
            LOGGER.info("Found configuration file: {}",DEFAULT_PROPERTIES_FILE_NAME);
            return Optional.of(new PropertyFileDefaultProvider(DEFAULT_PROPERTIES_FILE_NAME));
        }
        return Optional.empty();
    }

}
