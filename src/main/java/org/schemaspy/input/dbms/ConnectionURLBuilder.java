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

import org.schemaspy.util.DbSpecificOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author John Currier
 * @author Thomas Traude
 * @author Nils Petzaell
 * @author Daniel Watt
 */
public class ConnectionURLBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private ConnectionConfig connectionConfig;

    public ConnectionURLBuilder(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    public String build() {
        List<String> args = new ArrayList<>();
        args.addAll(connectionConfig.getRemainingArguments());

        String connectionURL = connectionConfig.getDatabaseTypeProperties().getProperty("connectionSpec");
        List<DbSpecificOption> options = connectionConfig.getDbSpecificConfig().getOptions();
        for (DbSpecificOption option : options) {
            option.setValue(getParam(args, option));

            LOGGER.debug("{}",option);

            // replace e.g. <host> with myDbHost
            connectionURL = connectionURL.replaceAll("<" + option.getName() + ">", option.getValue());
        }

        LOGGER.trace("connectionURL: {}", connectionURL);

        return connectionURL;
    }

    private String getParam(List<String> args, DbSpecificOption option) {
        String param = null;
        int paramIndex = args.indexOf("-" + option.getName());

        if (paramIndex < 0) {
            if ("db".equals(option.getName())) {
                param = connectionConfig.getDatabaseName();
            }
            if ("host".equals(option.getName())) {
                param = connectionConfig.getHost();
            }
            if ("port".equals(option.getName())) {
                param = Objects.nonNull(connectionConfig.getPort()) ? connectionConfig.getPort().toString() : null;
            }
            if ("hostOptionalPort".equals(option.getName())) {
                param = getHostOptionalPort();
            }
            if (param == null)
                throw new MissingParameterException(option.getName(), option.getDescription());
        } else {
            args.remove(paramIndex);
            param = args.get(paramIndex);
            args.remove(paramIndex);
        }

        return param;
    }

    private String getHostOptionalPort() {
        String hostOptionalPort = connectionConfig.getHost();
        if (hostOptionalPort == null) {
            throw new MissingParameterException("host", "host of database, may contain port");
        }
        String hostPortSeparator = connectionConfig.getDatabaseTypeProperties().getProperty("hostPortSeparator", ":");
        Integer port = connectionConfig.getPort();
        if (hostOptionalPort.contains(hostPortSeparator)) {
            return hostOptionalPort;
        }
        if (port != null) {
            return hostOptionalPort + hostPortSeparator + port;
        }
        return hostOptionalPort;
    }
}
