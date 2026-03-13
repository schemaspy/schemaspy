package org.schemaspy.output.dot.schemaspy.link;

public class FromBase implements NodeLink {

    private final NodeLink nodeLink;
    public FromBase(NodeLink nodeLink) {
        this.nodeLink = nodeLink;
    }

    @Override
    public String asString() {
        return "tables/" + nodeLink.asString();
    }
}
