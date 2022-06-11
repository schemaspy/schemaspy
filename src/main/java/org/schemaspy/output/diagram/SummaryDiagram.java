package org.schemaspy.output.diagram;

import java.io.File;

public class SummaryDiagram {

    private final DiagramProducer diagramProducer;
    private final File summaryDir;

    public SummaryDiagram(
            final DiagramProducer diagramProducer,
            final File summaryDir
    ) {
        this.diagramProducer = diagramProducer;
        this.summaryDir = summaryDir;
    }

    public DiagramResult generateSummaryDiagram(File dotFile, String diagramName) {
        try {
            File diagramFile = new File(summaryDir, diagramName + "." + diagramProducer.getDiagramFormat());
            String diagramMap = diagramProducer.generateDiagram(dotFile, diagramFile);
            return new DiagramResult(diagramFile.getName(), diagramMap, diagramProducer.getDiagramFormat());
        } catch (DiagramException diagramException) {
            throw new DiagramException("Failed to generate summary diagram", diagramException);
        }
    }
}