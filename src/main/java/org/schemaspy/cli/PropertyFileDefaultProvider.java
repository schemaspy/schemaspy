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

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;

import com.beust.jcommander.IDefaultProvider;

/**
 * Implementation of {@link IDefaultProvider} that provides values reading from
 * a {@link Properties} file.
 *
 * TODO JCommander already provides a
 * com.beust.jcommander.defaultprovider.PropertyFileDefaultProvider. But it
 * always reports "cannot find file on classpath" although it exists. Maybe open
 * an issue at the JCommander project?
 *
 * @author Thomas Traude
 * @author Daniel Watt
 * @author Nils Petzaell
 */
public class PropertyFileDefaultProvider implements IDefaultProvider {
	private final Properties properties;

	public PropertyFileDefaultProvider(String propertiesFilename) {
		Objects.requireNonNull(propertiesFilename);
		properties = loadProperties(propertiesFilename);
	}

	private static Properties loadProperties(String path) {
		try {
			String contents = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
			Properties properties = new Properties();
			// Replace backslashes with double backslashes to escape windows path separator.
			// Example input: schemaspy.o=C:\tools\schemaspy\output
			properties.load(new StringReader(contents.replace("\\", "\\\\")));
			return properties;
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not find or load properties file: " + path, e);
		}
	}

	@Override
	public String getDefaultValueFor(String optionName) {
		return properties.getProperty(optionName);
	}
}
