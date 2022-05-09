package org.schemaspy.output.dot.schemaspy.graph;

/**
 * Abstracts pair-wise relationships between objects.
 */
public interface Graph {

    /**
     * Asks the graph to represent itself in the DOT language.
     * @return A textual representation of the graph.
     */
    String dot();
}
