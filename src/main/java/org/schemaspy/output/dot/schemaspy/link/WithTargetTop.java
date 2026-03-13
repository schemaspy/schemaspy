package org.schemaspy.output.dot.schemaspy.link;

public class WithTargetTop implements NodeLink {
    private final NodeLink nodeLink;
    public WithTargetTop(NodeLink nodeLink) {
        this.nodeLink = nodeLink;
    }

    @Override
    public String asString() {
        return "    URL=\"" +nodeLink.asString()  + "\"" +
                System.lineSeparator() +
                "    target=\"_top\"" +
                System.lineSeparator();
    }
}
