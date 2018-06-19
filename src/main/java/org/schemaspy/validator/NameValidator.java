/*
 * Copyright (C) 2004 - 2011, 2014 John Currier
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2017 Daniel Watt
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
package org.schemaspy.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by rkasa on 2016-12-10.
 *  "macro" to validate that a table is somewhat valid
 *
 * @author John Currier
 * @author Rafal Kasa
 * @author Daniel Watt
 * @author Nils Petzaell
 */
public class NameValidator {
    private final String clazz;
    private final Pattern include;
    private final Pattern exclude;
    private final Set<String> validTypes;

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * @param clazz table or view
     * @param include
     * @param exclude
     * @param validTypes
     */
    public NameValidator(String clazz, Pattern include, Pattern exclude, String[] validTypes) {
        this.clazz = clazz;
        this.include = include;
        this.exclude = exclude;
        this.validTypes = new HashSet<>();
        for (String type : validTypes) {
            this.validTypes.add(type.toUpperCase());
        }
    }

    /**
     * Returns <code>true</code> if the table/view name is deemed "valid"
     *
     * @param name name of the table or view
     * @param type type as returned by metadata.getTables():TABLE_TYPE
     * @return
     */
    public boolean isValid(String name, String type) {
        // some databases (MySQL) return more than we wanted
        if (!validTypes.contains(type.toUpperCase()))
            return false;

        if (exclude.matcher(name).matches()) {
            LOGGER.debug("Excluding {} {}: matches exclusion pattern '{}'", clazz, name, exclude);
            return false;
        }

        boolean valid = include.matcher(name).matches();
        if (valid) {
            LOGGER.debug("Including {} {}: matches inclusion pattern '{}'", clazz, name, include);
        } else {
            LOGGER.debug("Excluding {} {}: doesn't match inclusion pattern '{}'", clazz, name, include);
        }
        return valid;
    }
}
