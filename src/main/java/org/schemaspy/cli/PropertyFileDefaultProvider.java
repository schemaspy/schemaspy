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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.Properties;

/**
 * Implementation of {@link IDefaultProvider} that provides values reading from a {@link Properties} file.
 *
 * TODO
 * JCommander already provides a com.beust.jcommander.defaultprovider.PropertyFileDefaultProvider.
 * But it always reports "cannot find file on classpath" although it exists. Maybe open an issue at the JCommander project?
 * @author Thomas Traude
 * @author Daniel Watt
 * @author Nils Petzaell
 */
public class PropertyFileDefaultProvider implements IDefaultProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Properties properties;

    public PropertyFileDefaultProvider(String propertiesFilename) {
        Objects.requireNonNull(propertiesFilename);
        properties = loadProperties(propertiesFilename);
    }

    private static Properties loadProperties(String path) {
        try (Reader reader = new InputStreamReader(new FileInputStream(path), "UTF-8")){
            Properties properties = new Properties();
            String contents = FileCopyUtils.copyToString(reader);
            // Replace backslashes with double backslashes to escape windows path separator.
            // Example input: schemaspy.o=C:\tools\schemaspy\output
            properties.load(new StringReader(contents.replace("\\", "\\\\")));
            return properties;
        } catch (IOException e) {
            LOGGER.error("File not found: {}", path, e);
            throw new IllegalArgumentException("Could not find or load properties file: " + path, e);
        }
    }

    @Override
    public String getDefaultValueFor(String optionName) {
        return properties.getProperty(optionName);
    }
}
