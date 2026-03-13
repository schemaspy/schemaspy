package org.schemaspy.output.dot.schemaspy.link;

import org.schemaspy.model.Table;

public class WithTargetTopTableNodeLinkFactory implements TableNodeLinkFactory {

    private final TableNodeLinkFactory tableNodeLinkFactory;

    public WithTargetTopTableNodeLinkFactory(TableNodeLinkFactory tableNodeLinkFactory) {
        this.tableNodeLinkFactory = tableNodeLinkFactory;
    }

    @Override
    public NodeLink nodeLink(Table table) {
        return new WithTargetTop(tableNodeLinkFactory.nodeLink(table));
    }
}
