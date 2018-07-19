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
package org.schemaspy.util;

import org.schemaspy.Config;
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

    private final Config config;
    private final Properties dbType;

    /**
     * @param config
     * @param properties
     */
    public ConnectionURLBuilder(Config config, Properties properties) {
        this.config = config;
        this.dbType = properties;
    }

    public String build() {
        List<String> args = getArgs();
        List<String> remaining = config.getRemainingParameters();
        args.addAll(remaining);

        String connectionURL = dbType.getProperty("connectionSpec");
        DbSpecificConfig dbConfig = new DbSpecificConfig(config.getDbProperties());
        for (DbSpecificOption option : dbConfig.getOptions()) {
            option.setValue(getParam(args, option));

            LOGGER.debug("{}",option.toString());

            // replace e.g. <host> with myDbHost
            connectionURL = connectionURL.replaceAll("\\<" + option.getName() + "\\>", option.getValue());
        }

        for (DbSpecificOption option : dbConfig.getOptions()) {
            int idx = remaining.indexOf("-" + option.getName());
            if (idx >= 0) {
                remaining.remove(idx);  // -paramKey
                remaining.remove(idx);  // paramValue
            }
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
