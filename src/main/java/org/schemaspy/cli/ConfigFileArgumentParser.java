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
import org.schemaspy.Config;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Thomas Traude
 */
@Component
public class ConfigFileArgumentParser {

    public Optional<String> parseConfigFileArgumentValue(String... args) {
        Objects.requireNonNull(args, "Command line arguments must not be null.");

        JCommander jCommander = JCommander.newBuilder()
                .acceptUnknownOptions(true)
                .programName("java -jar " + Config.getLoadedFromJar())
                .columnSize(120)
                .build();

        ConfigFileArgument configFileArgument = new ConfigFileArgument();
        jCommander.addObject(configFileArgument);
        jCommander.parse(args);
        return configFileArgument.getConfigFile();
    }
}
