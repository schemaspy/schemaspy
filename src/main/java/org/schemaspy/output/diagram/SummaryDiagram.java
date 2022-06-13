package org.schemaspy.output.diagram;

import java.io.File;

public class SummaryDiagram {

    private final Renderer renderer;
    private final File summaryDir;

    public SummaryDiagram(
            final Renderer renderer,
            final File summaryDir
    ) {
        this.renderer = renderer;
        this.summaryDir = summaryDir;
    }

    public DiagramResult generateSummaryDiagram(File dotFile, String diagramName) {
        try {
            File diagramFile = new File(summaryDir, diagramName + "." + renderer.format());
            String diagramMap = renderer.render(dotFile, diagramFile);
            return new DiagramResult(diagramFile.getName(), diagramMap, renderer.format());
        } catch (RenderException diagramException) {
            throw new RenderException("Failed to generate summary diagram", diagramException);
        }
    }
}