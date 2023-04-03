package org.schemaspy.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.regex.Pattern;

public final class InclusionValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final String clazz;
    private final Pattern include;

    public InclusionValidator(final String clazz, final Pattern include) {
        this.clazz = clazz;
        this.include = include;
    }

    public boolean isIncluded(String name) {
        boolean valid = this.include.matcher(name).matches();
        if (valid) {
            LOGGER.debug("Including {} {}: matches inclusion pattern '{}'", this.clazz, name, this.include);
        } else {
            LOGGER.debug("Excluding {} {}: doesn't match inclusion pattern '{}'", this.clazz, name, this.include);
        }
        return valid;
    }
}
