package org.schemaspy.validator;

import java.util.Set;

public final class DatabasetypeValidator {

    private final Set<String> validTypes;

    public DatabasetypeValidator(final Set<String> validTypes) {
        this.validTypes = validTypes;
    }

    public boolean isValid(String type) {
        // some databases (MySQL) return more than we wanted
        if (!validTypes.contains(type.toUpperCase()))
            return false;
        return true;
    }
}
