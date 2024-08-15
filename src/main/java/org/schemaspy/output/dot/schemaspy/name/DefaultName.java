package org.schemaspy.output.dot.schemaspy.name;

import org.schemaspy.util.naming.Name;

/**
 * Provides a default name for a graph.
 */
public final class DefaultName implements Name {

    @Override
    public String value() {
        return "RelationshipsDiagram";
    }
}
