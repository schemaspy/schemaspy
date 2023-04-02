package org.schemaspy.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.regex.Pattern;

public final class ExclusionValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final String clazz;
    private final Pattern exclude;

    /**
     * Checks the name against a deny-pattern.
     */
    public ExclusionValidator(final String clazz, final Pattern exclude) {
        this.clazz = clazz;
        this.exclude = exclude;
    }

    public boolean isValid(String name) {
        if (this.exclude.matcher(name).matches()) {
            LOGGER.debug("Excluding {} {}: matches exclusion pattern '{}'", this.clazz, name, this.exclude);
            return false;
        }
        return true;
    }
}
