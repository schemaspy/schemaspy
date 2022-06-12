package org.schemaspy.output.diagram;

import java.io.File;

public class TableDiagram {

    private final Renderer renderer;
    private final File tablesDir;

    public TableDiagram(
            final Renderer renderer,
            final File tablesDir
    ) {
        this.renderer = renderer;
        this.tablesDir = tablesDir;
    }

    public DiagramResult generateTableDiagram(File dotFile, String diagramName) {
        try {
            File diagramFile = new File(tablesDir, diagramName + "." + renderer.format());
            String diagramMap = renderer.render(dotFile, diagramFile);
            return new DiagramResult(diagramFile.getName(), diagramMap, renderer.format());
        } catch (DiagramException diagramException) {
            throw new DiagramException("Failed to generate Table diagram", diagramException);
        }
    }
}

