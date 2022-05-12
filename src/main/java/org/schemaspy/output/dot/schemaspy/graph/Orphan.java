package org.schemaspy.output.dot.schemaspy.graph;

import org.schemaspy.output.dot.schemaspy.Header;
import org.schemaspy.output.dot.schemaspy.Node;
import org.schemaspy.output.dot.schemaspy.name.Name;

public final class Orphan implements Graph {

    private final Graph origin;

    public Orphan(final Name name, final Header header, final Node node) {
        this(new Digraph(name, header, node));
    }

    public Orphan(final Graph origin) {
        this.origin = origin;
    }

    @Override
    public String dot() {
        return this.origin.dot();
    }
}
