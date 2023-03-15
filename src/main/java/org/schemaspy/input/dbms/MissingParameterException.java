package org.schemaspy.input.dbms;

/**
 * Thrown to indicate that a required parameter is missing
 */
public class MissingParameterException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public MissingParameterException(String paramId, String description) {
        super("Required parameter '" + paramId + "' " +
            (description == null ? "" : "(" + description + ") ") +
            "was not specified." +
            "  It is required for this database type.");
    }
}
