package org.schemaspy.output.diagram;

import java.io.File;

public class TableDiagram {

    private final DiagramProducer diagramProducer;
    private final File tablesDir;

    public TableDiagram(
            final DiagramProducer diagramProducer,
            final File tablesDir
    ) {
        this.diagramProducer = diagramProducer;
        this.tablesDir = tablesDir;
    }

    public DiagramResult generateTableDiagram(File dotFile, String diagramName) {
        try {
            File diagramFile = new File(tablesDir, diagramName + "." + diagramProducer.getDiagramFormat());
            String diagramMap = diagramProducer.generateDiagram(dotFile, diagramFile);
            return new DiagramResult(diagramFile.getName(), diagramMap, diagramProducer.getDiagramFormat());
        } catch (DiagramException diagramException) {
            throw new DiagramException("Failed to generate Table diagram", diagramException);
        }
    }
}

