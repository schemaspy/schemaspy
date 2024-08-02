package org.schemaspy.output.dot.schemaspy.link;

import org.schemaspy.model.Table;
import org.schemaspy.output.diagram.DiagramFactory;

public class RelativeToDiagramTableNodeLinkFactory implements TableNodeLinkFactory{

    private final DiagramFactory diagramFactory;
    private final TableNodeLinkFactory tableNodeLinkFactory;

    public RelativeToDiagramTableNodeLinkFactory(DiagramFactory diagramFactory, TableNodeLinkFactory tableNodeLinkFactory) {
        this.diagramFactory = diagramFactory;
        this.tableNodeLinkFactory = tableNodeLinkFactory;
    }

    @Override
    public NodeLink nodeLink(Table table) {
        if ("svg".equalsIgnoreCase(diagramFactory.getDiagramFormat())) {
            if(table.isRemote()) {
                return new RemoteRelativeToDiagram(table, new HtmlWithEncodedName(table));
            }
            return new RelativeToDiagram(new HtmlWithEncodedName(table));
        }
        return tableNodeLinkFactory.nodeLink(table);
    }
}
