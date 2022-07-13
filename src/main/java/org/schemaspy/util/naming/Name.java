package org.schemaspy.util.naming;

/**
 * Abstracts a means to refer to something.
 */
public interface Name {

    /**
     * Asks the name to represent itself in text.
     * @return A textual representation of the name.
     */
    String value();
}
