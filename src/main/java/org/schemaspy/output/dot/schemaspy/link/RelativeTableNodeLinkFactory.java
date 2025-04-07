package org.schemaspy.output.dot.schemaspy.link;

import org.schemaspy.model.Table;

public class RelativeTableNodeLinkFactory implements TableNodeLinkFactory {

    @Override
    public NodeLink nodeLink(Table table) {
        if (table.isRemote()) {
            return new RemoteRelative(table, new HtmlWithEncodedName(table));
        }
        return new Relative(new HtmlWithEncodedName(table));
    }
}
