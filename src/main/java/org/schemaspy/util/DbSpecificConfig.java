/*
 * Copyright (C) 2004-2010 John Currier
 * Copyright (C) 2017 Nils Petzaell
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
import java.util.StringTokenizer;

/**
 * Configuration of a specific type of database (as specified by -t)
 *
 * @author John Currier
 * @author Nils Petzaell
 */
public class DbSpecificConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final String type;
    private String description;
    private final List<DbSpecificOption> options = new ArrayList<>();
    private final Config config = new Config();

    /**
     * Construct an instance with configuration options of the specified database type
     *
     * @param dbType
     */
    public DbSpecificConfig(String dbType) {
        type = dbType;
        Properties props = config.determineDbProperties(dbType);
        description = props.getProperty("description");
        loadOptions(props);
    }

    public DbSpecificConfig(String dbType, Properties props) {
        type = dbType;
        description = props.getProperty("description");
        loadOptions(props);
    }

    /**
     * Resolve the options specified by connectionSpec into {@link DbSpecificOption}s.
     *
     * @param properties
     */
    private void loadOptions(Properties properties) {
        boolean inParam = false;

        StringTokenizer tokenizer = new StringTokenizer(properties.getProperty("connectionSpec"), "<>", true);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if ("<".equals(token)) {
                inParam = true;
            } else if (">".equals(token)) {
                inParam = false;
            } else {
                if (inParam) {
                    String desc = properties.getProperty(token);
                    options.add(new DbSpecificOption(token, desc));
                }
            }
        }
    }

    /**
     * Returns a {@link List} of {@link DbSpecificOption}s that are applicable to the
     * specified database type.
     *
     * @return
     */
    public List<DbSpecificOption> getOptions() {
        return options;
    }

    /**
     * Return the generic configuration associated with this DbSpecificCofig
     *
     * @return
     */
    public Config getConfig() {
        return config;
    }

    /**
     * Dump usage details associated with the associated type of database
     */
    public void dumpUsage() {
        LOGGER.info(description);
        getOptions().stream().map(option ->
                        "   -" +
                        option.getName() +
                        " " +
                        (
                                option.getDescription() != null ?
                                "  \t\t" + option.getDescription() :
                                ""
                        )
                ).forEach(LOGGER::info);
    }

    /**
     * Return description of the associated type of database
     */
    @Override
    public String toString() {
        return description;
    }
}
