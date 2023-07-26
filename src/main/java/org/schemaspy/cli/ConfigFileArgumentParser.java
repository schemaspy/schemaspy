/*
 * Copyright (C) 2017 Thomas Traude
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

import com.beust.jcommander.JCommander;

import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Thomas Traude
 */
public class ConfigFileArgumentParser {

    private final String[] args;
    private final JCommander jCommander;
    private final ConfigFileArgument configFileArgument = new ConfigFileArgument();

    public ConfigFileArgumentParser(String...args) {
        this.args = Objects.requireNonNull(args, "Command line arguments must not be null.");
        this.jCommander = JCommander.newBuilder()
                .acceptUnknownOptions(true)
                .programName("java -jar \"" + Paths.get("").toAbsolutePath().relativize(new SchemaSpyJarFile().path()) + "\"")
                .columnSize(120)
                .build();
        jCommander.addObject(configFileArgument);
    }

    public Optional<String> configFile() {
        jCommander.parse(args);
        return configFileArgument.getConfigFile();
    }
}
