/*
 * Copyright (C) 2004-2011 John Currier
 * Copyright (C) 2017 Thomas Traude
 * Copyright (C) 2017 Nils Petzaell
 * Copyright (C) 2017 Daniel Watt
 *
 * This file is a part of the SchemaSpy project (http://schemaspy.org).
 *
 * SchemaSpy is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.schemaspy.input.dbms;

import org.schemaspy.Config;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.input.dbms.config.SimplePropertiesResolver;
import org.schemaspy.util.DbSpecificConfig;
import org.schemaspy.util.DbSpecificOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author John Currier
 * @author Thomas Traude
 * @author Nils Petzaell
 * @author Daniel Watt
 */
public class ConnectionURLBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final DbSpecificConfig dbConfig;
    private final Config config;
    private final Properties dbType;

    /**
     * @param config
     * @param properties
     */
    public ConnectionURLBuilder(Config config, Properties properties) {
        this(
            new DbSpecificConfig(
                config.getDbType(),
                config.getDbProperties()
            ),
            config,
            properties
        );
    }

    public ConnectionURLBuilder(
            final CommandLineArguments commandLineArguments,
            final Config config,
            final Properties properties
    ) {
        this(
            new DbSpecificConfig(
                commandLineArguments.getDatabaseType(),
                new SimplePropertiesResolver().getDbProperties(commandLineArguments.getDatabaseType())
            ),
            config,
            properties
        );
    }

    public ConnectionURLBuilder(DbSpecificConfig dbConfig, Config config, Properties properties) {
        this.dbConfig = dbConfig;
        this.config = config;
        this.dbType = properties;
    }

    public String build() {
        List<String> args = getArgs();
        args.addAll(config.getRemainingParameters());

        String connectionURL = dbType.getProperty("connectionSpec");
        for (DbSpecificOption option : this.dbConfig.getOptions()) {
            option.setValue(getParam(args, option));

            LOGGER.debug("{}",option);

            // replace e.g. <host> with myDbHost
            connectionURL = connectionURL.replaceAll("\\<" + option.getName() + "\\>", option.getValue());
        }

        LOGGER.trace("connectionURL: {}", connectionURL);

        return connectionURL;
    }

    private List<String> getArgs() {
        List<String> args = new ArrayList<>();

        for (String key : config.getDbSpecificOptions().keySet()) {
            args.add((key.startsWith("-") ? "" : "-") + key);
            args.add(config.getDbSpecificOptions().get(key));
        }
        return args;
    }

    private String getParam(List<String> args, DbSpecificOption option) {
        String param = null;
        int paramIndex = args.indexOf("-" + option.getName());

        if (paramIndex < 0) {
            if (config != null)
                param = config.getParam(option.getName());  // not in args...might be one of
            // the common db params
            if ("hostOptionalPort".equals(option.getName())) {
                param = getHostOptionalPort();
            }
            if (param == null)
                throw new Config.MissingRequiredParameterException(option.getName(), option.getDescription(), true);
        } else {
            args.remove(paramIndex);
            param = args.get(paramIndex);
            args.remove(paramIndex);
        }

        return param;
    }

    private String getHostOptionalPort() {
        String hostOptionalPort = config.getHost();
        if (hostOptionalPort == null) {
            throw new Config.MissingRequiredParameterException("host", "host of database, may contain port", true);
        }
        String hostPortSeparator = dbType.getProperty("hostPortSeparator", ":");
        Integer port = config.getPort();
        if (hostOptionalPort.contains(hostPortSeparator)) {
            return hostOptionalPort;
        }
        if (port != null) {
            return hostOptionalPort + hostPortSeparator + port;
        }
        return hostOptionalPort;
    }
}
