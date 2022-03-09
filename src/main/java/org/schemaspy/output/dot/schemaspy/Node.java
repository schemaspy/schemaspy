package org.schemaspy.output.dot.schemaspy;

/**
 * Represents a node in the dot language.
 */
public interface Node {

    /**
     * Asks the node to represent itself in text.
     * @return A textual representation of the node.
     */
    String value();
}
