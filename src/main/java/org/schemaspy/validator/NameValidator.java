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
 */
public class NameValidator {
    private final String clazz;
    private final Pattern include;
    private final Pattern exclude;
    private final Set<String> validTypes;

    private final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
        for (String type : validTypes)
        {
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

        // Oracle 10g introduced problematic flashback tables
        // with bizarre illegal names
        if (name.contains("$")) {
            LOGGER.debug("Excluding {} {}: embedded $ implies illegal name", clazz, name);
            return false;
        }

        if (exclude.matcher(name).matches()) {
            LOGGER.debug("Excluding {} {}: matches exclusion pattern \"{}" + '"', clazz, name, exclude);
            return false;
        }

        boolean valid = include.matcher(name).matches();
        if (valid) {
            LOGGER.debug("Including {} {}: matches inclusion pattern \"{}" + '"', clazz, name, include);
        } else {
            LOGGER.debug("Excluding {} {}: doesn't match inclusion pattern \"{}" + '"', clazz, name, include);
        }
        return valid;
    }
}
