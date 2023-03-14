package org.schemaspy.input.dbms;

/**
 * Thrown to indicate that a required parameter is missing
 */
public class MissingRequiredParameterException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final boolean dbTypeSpecific;

    public MissingRequiredParameterException(String paramId, boolean dbTypeSpecific) {
        this(paramId, null, dbTypeSpecific);
    }

    public MissingRequiredParameterException(String paramId, String description, boolean dbTypeSpecific) {
        super("Required parameter '" + paramId + "' " +
            (description == null ? "" : "(" + description + ") ") +
            "was not specified." +
            (dbTypeSpecific ? "  It is required for this database type." : ""));
        this.dbTypeSpecific = dbTypeSpecific;
    }

    public boolean isDbTypeSpecific() {
        return dbTypeSpecific;
    }
}
