package org.schemaspy.output.dot.schemaspy.link;

import org.schemaspy.model.Table;

public interface TableNodeLinkFactory {

    NodeLink nodeLink(Table table);
}
