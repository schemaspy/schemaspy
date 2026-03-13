package org.schemaspy.output.dot.schemaspy.link;

public class RelativeToDiagram implements NodeLink {

    private final NodeLink nodeLink;
    public RelativeToDiagram(NodeLink nodeLink) {
        this.nodeLink = nodeLink;
    }

    @Override
    public String asString() {
        return "../.." + TABLES_PATH + nodeLink.asString();
    }
}
