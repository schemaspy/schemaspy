package org.schemaspy.output.dot.schemaspy.link;

import org.schemaspy.model.Table;

public class FromBaseTableNodeLinkFactory implements TableNodeLinkFactory {

    @Override
    public NodeLink nodeLink(Table table) {
        if (table.isRemote()) {
            return new RemoteFromBase(table, new HtmlWithEncodedName(table));
        }
        return new FromBase(new HtmlWithEncodedName(table));
    }
}
