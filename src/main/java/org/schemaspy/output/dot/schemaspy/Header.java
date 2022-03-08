package org.schemaspy.output.dot.schemaspy;

/**
 * Represents a header in the dot language.
 */
public interface Header {

    /**
     * Asks the header to represent itself in text.
     * @return A textual representation of the header.
     */
    String value();
}
