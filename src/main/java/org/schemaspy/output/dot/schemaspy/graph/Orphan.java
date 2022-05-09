package org.schemaspy.output.dot.schemaspy.graph;

import org.schemaspy.output.dot.schemaspy.Header;
import org.schemaspy.output.dot.schemaspy.Node;
import org.schemaspy.output.dot.schemaspy.name.Name;

public final class Orphan implements Graph {

    private final Name name;
    private final Header header;
    private final Node node;

    public Orphan(final Name name, final Header header, final Node node) {
        this.name = name;
        this.header = header;
        this.node = node;
    }

    @Override
    public String dot() {
        return String.format(
            "digraph \"%s\" { %s %s }",
            this.name.value(),
            this.header.value(),
            this.node.value()
        );
    }
}
