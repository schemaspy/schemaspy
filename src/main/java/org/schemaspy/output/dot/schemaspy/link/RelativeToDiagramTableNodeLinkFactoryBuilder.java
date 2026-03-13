package org.schemaspy.output.dot.schemaspy.link;

import org.schemaspy.output.diagram.DiagramFactory;

public class RelativeToDiagramTableNodeLinkFactoryBuilder {
    private final DiagramFactory diagramFactory;

    public RelativeToDiagramTableNodeLinkFactoryBuilder(DiagramFactory diagramFactory) {
        this.diagramFactory = diagramFactory;
    }

    public TableNodeLinkFactory withTableNodeLinkFactory(TableNodeLinkFactory tableNodeLinkFactory) {
        return new RelativeToDiagramTableNodeLinkFactory(diagramFactory, tableNodeLinkFactory);
    }
}
