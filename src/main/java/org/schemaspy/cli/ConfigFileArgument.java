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

import com.beust.jcommander.Parameter;

import java.util.Optional;

/**
 * This class only contains the value for the optioanl <code>configFile</code> command line argument.
 * When setting the configFile parameter the user can provide an external properties file
 * that holds values for all other configuration parameter.
 * <p>
 * Example:
 * <p>
 * Command line call:
 * <pre>
 *     java -jar schemaspy.jar -configFile myconfig.properties
 * </pre>
 * <p>
 * Content of myconfig.properties:
 * <pre>
 *     schemaspy.databaseType=mysql
 *     schemaspy.outputDirectory=schemaspy-report
 *     schemaspy.user=MyUser
 *     ...
 * </pre>
 * <p>
 * Schemaspy checks for the presence <code>configFile</code> argument before any other arguments defined in {@link CommandLineArguments}.
 * @author Thomas Traude
 */
public final class ConfigFileArgument {

    @Parameter(names = "-configFile")
    private String configFile;

    public Optional<String> getConfigFile() {
        return Optional.ofNullable(configFile);
    }
}
