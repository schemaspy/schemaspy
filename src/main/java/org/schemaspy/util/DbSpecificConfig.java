/*
 * Copyright (C) 2004-2010 John Currier
 * Copyright (C) 2017 Wojciech Kasa
 * Copyright (C) 2017 Thomas Traude
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Configuration of a specific type of database (as specified by -t)
 *
 * @author John Currier
 * @author Wojciech Kasa
 * @author Thomas Traude
 * @author Nils Petzaell
 */
public class DbSpecificConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final Pattern OPTION_PATTER = Pattern.compile("<([a-zA-Z0-9.\\-_]+)>");
    private static final String DUMP_FORMAT = "   -%s   \t\t%s";

    private String description;
    private final List<DbSpecificOption> options = new ArrayList<>();

    public DbSpecificConfig(Properties props) {
        description = props.getProperty("description");
        loadOptions(props);
    }

    /**
     * Resolve the options specified by connectionSpec into {@link DbSpecificOption}s.
     *
     * @param properties
     */
    private void loadOptions(Properties properties) {
        Set<String> optionsFound = findOptions(properties.getProperty("connectionSpec"));
        optionsFound.stream().forEachOrdered(optionName -> {
            String desc = properties.getProperty(optionName);
            options.add(new DbSpecificOption(optionName, desc));
        });
    }

    private static Set<String> findOptions(String connectionSpec) {
        Set<String> optionsFound = new LinkedHashSet<>();
        Matcher matcher = OPTION_PATTER.matcher(connectionSpec);
        while(matcher.find()) {
            optionsFound.add(matcher.group(1));
        }
        return optionsFound;
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
     * Dump usage details associated with the associated type of database
     */
    public void dumpUsage() {
        LOGGER.info(description);
        getOptions().stream().flatMap(option -> {
            if ("hostOptionalPort".equals(option.getName())) {
                return Stream.of(
                        String.format(DUMP_FORMAT, "host", "host of database, may contain port"),
                        String.format(DUMP_FORMAT, "port", "optional port if not default")
                );
            } else {
                return Stream.of(
                        String.format(DUMP_FORMAT, option.getName(), getDescription(option))
                );
            }
        }).forEach(LOGGER::info);
    }

    private static String getDescription(DbSpecificOption option) {
        return Objects.isNull(option.getDescription()) ? "" : option.getDescription();
    }

    /**
     * Return description of the associated type of database
     */
    @Override
    public String toString() {
        return description;
    }
}
