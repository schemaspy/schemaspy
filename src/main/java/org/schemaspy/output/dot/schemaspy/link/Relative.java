package org.schemaspy.output.dot.schemaspy.link;

public class Relative implements NodeLink {

    private final NodeLink nodeLink;
    public Relative(NodeLink nodeLink) {
        this.nodeLink = nodeLink;
    }

    @Override
    public String asString() {
        return nodeLink.asString();
    }
}
