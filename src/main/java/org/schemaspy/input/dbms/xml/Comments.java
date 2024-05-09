package org.schemaspy.input.dbms.xml;

/**
 * Abstracts a means to obtain database comments.
 */
public interface Comments {

    /**
     * Asks the comments to represent themselves in text.
     * @return A textual representation of the comments.
     */
    String value();
}
