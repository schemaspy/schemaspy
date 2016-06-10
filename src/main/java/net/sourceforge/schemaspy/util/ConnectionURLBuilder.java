/*
 * This file is a part of the SchemaSpy project (http://schemaspy.sourceforge.net).
 * Copyright (C) 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011 John Currier
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
package net.sourceforge.schemaspy.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import net.sourceforge.schemaspy.Config;

/**
 * @author John Currier
 */
public class ConnectionURLBuilder {
    private final String connectionURL;
    private final List<DbSpecificOption> options;
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * @param config
     * @param properties
     */
    public ConnectionURLBuilder(Config config, Properties properties) {
        List<String> opts = new ArrayList<String>();

        for (String key : config.getDbSpecificOptions().keySet()) {
            opts.add((key.startsWith("-") ? "" : "-") + key);
            opts.add(config.getDbSpecificOptions().get(key));
        }
        opts.addAll(config.getRemainingParameters());

        DbSpecificConfig dbConfig = new DbSpecificConfig(config.getDbType());
        options = dbConfig.getOptions();
        connectionURL = buildUrl(opts, properties, config);

        List<String> remaining = config.getRemainingParameters();

        for (DbSpecificOption option : options) {
            int idx = remaining.indexOf("-" + option.getName());
            if (idx >= 0) {
                remaining.remove(idx);  // -paramKey
                remaining.remove(idx);  // paramValue
            }
        }

        logger.config("connectionURL: " + connectionURL);
    }

    private String buildUrl(List<String> args, Properties properties, Config config) {
        String connectionSpec = properties.getProperty("connectionSpec");

        for (DbSpecificOption option : options) {
            option.setValue(getParam(args, option, config));

            logger.fine(option.toString());

            // replace e.g. <host> with myDbHost
            connectionSpec = connectionSpec.replaceAll("\\<" + option.getName() + "\\>", option.getValue().toString());
        }

        return connectionSpec;
    }

    public String getConnectionURL() {
        return connectionURL;
    }

    /**
     * Returns a {@link List} of populated {@link DbSpecificOption}s that are applicable to
     * the specified database type.
     *
     * @return
     */
    public List<DbSpecificOption> getOptions() {
        return options;
    }

    private String getParam(List<String> args, DbSpecificOption option, Config config) {
        String param = null;
        int paramIndex = args.indexOf("-" + option.getName());

        if (paramIndex < 0) {
            if (config != null)
                param = config.getParam(option.getName());  // not in args...might be one of
                                                            // the common db params
            if (param == null)
                throw new Config.MissingRequiredParameterException(option.getName(), option.getDescription(), true);
        } else {
            args.remove(paramIndex);
            param = args.get(paramIndex).toString();
            args.remove(paramIndex);
        }

        return param;
    }
}
