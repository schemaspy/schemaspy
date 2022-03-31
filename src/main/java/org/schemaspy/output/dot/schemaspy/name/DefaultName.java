package org.schemaspy.output.dot.schemaspy.name;

/**
 * Provides a default name for a graph.
 */
public final class DefaultName implements Name {

    public DefaultName() { }

    @Override
    public String value() {
        return "RelationshipsDiagram";
    }
}
