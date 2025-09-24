package org.schemaspy.output.dot.schemaspy.link;

import org.schemaspy.model.Table;
import org.schemaspy.view.FileNameGenerator;

public class RemoteFromBase implements NodeLink {

    private final Table table;
    private final NodeLink nodeLink;
    public RemoteFromBase(Table table, NodeLink nodeLink) {
        this.table = table;
        this.nodeLink = nodeLink;
    }
    @Override
    public String asString() {
        return "../" + new FileNameGenerator().generate(table.getContainer()) + TABLES_PATH + nodeLink.asString();
    }
}
