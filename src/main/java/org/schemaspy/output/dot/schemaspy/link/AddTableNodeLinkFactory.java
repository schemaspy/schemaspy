package org.schemaspy.output.dot.schemaspy.link;

import org.schemaspy.model.Table;

public class AddTableNodeLinkFactory implements TableNodeLinkFactory{

    private final boolean multiSchema;
    private final TableNodeLinkFactory tableNodeLinkFactory;

    public AddTableNodeLinkFactory(boolean multiSchema, TableNodeLinkFactory tableNodeLinkFactory) {
        this.multiSchema = multiSchema;
        this.tableNodeLinkFactory = tableNodeLinkFactory;
    }
    @Override
    public NodeLink nodeLink(Table table) {
        if (!table.isRemote() || multiSchema) {
            return tableNodeLinkFactory.nodeLink(table);
        }
        return new NoNodeLink();
    }
}
