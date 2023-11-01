/*
 * Copyright (C) 2017 Thomas Traude
 * Copyright (C) 2017 Daniel Watt
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
import com.beust.jcommander.ParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.Optional;

/**
 * This class creates instances of {@link CombinedDefaultProvider}
 * Which might contain properties from -configFile or schemaspy.properties
 * and
 * {@link EnvDefaultProvider}.
 * This class creates instances of {@link PropertyFileDefaultProvider} based on a name of the {@link java.util.Properties} file.
 * @author Thomas Traude
 * @author Daniel Watt
 */
public class DefaultProviderFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String DEFAULT_PROPERTIES_FILE_NAME = "schemaspy.properties";

    private final Optional<String> propertiesFileName;
    private final String defaultPropertiesFileName;

    public DefaultProviderFactory(Optional<String> propertiesFileName) {
        this(propertiesFileName, DEFAULT_PROPERTIES_FILE_NAME);
    }

    public DefaultProviderFactory(Optional<String> propertiesFileName, String defaultPropertiesFileName) {
        this.propertiesFileName = propertiesFileName;
        this.defaultPropertiesFileName = defaultPropertiesFileName;
    }

    /**
     * Return a {@link CombinedDefaultProvider} which at least contains EnvDefaultProvider and if available a
     * PropertyFileDefaultProvider base on -configFile or schemaspy.properties
     * <p>
     * If for the given propertiesFilename there exists no file the method will exit the application.
     * <p>
     * If the given propertiesFilename is null it falls back to the {@link #DEFAULT_PROPERTIES_FILE_NAME}.
     * If a properties file exists it will return CombinedDefaultProvider search order Properties then Env
     * If this file does not exist the method returns CombinedDefaultProvider with only EnvDefaultProvider
     *
     * @return IDefaultProvider
     */
    public IDefaultProvider defaultProvider() {
        if (propertiesFileName.isPresent()) {
            String fileName = propertiesFileName.get();
            if (exists(fileName)) {
				LOGGER.info("Found configuration file: {}", fileName);
                return new CombinedDefaultProvider(
                        new PropertyFileDefaultProvider(fileName),
                        new EnvDefaultProvider()
                );
            } else {
                throw new ParameterException("Could not find -configFile: " + fileName);
            }
        }

        if (exists(defaultPropertiesFileName)) {
            LOGGER.info("Found default configuration file: {}",defaultPropertiesFileName);
            return new CombinedDefaultProvider(
                    new PropertyFileDefaultProvider(defaultPropertiesFileName),
                    new EnvDefaultProvider()
            );
        }
        return new CombinedDefaultProvider(new EnvDefaultProvider());
    }

    private static boolean exists(String fileName) {
        return new File(fileName).exists();
    }

}
