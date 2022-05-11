package org.schemaspy.output.dot.schemaspy.graph;

/**
 * Abstracts graph elements.
 */
public interface Element {

    /**
     * Asks the element to represent itself in the DOT language.
     * @return A textual representation of the element.
     */
    String value();
}
