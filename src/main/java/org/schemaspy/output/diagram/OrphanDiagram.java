package org.schemaspy.output.diagram;

import java.io.File;

public class OrphanDiagram {
    private final DiagramProducer diagramProducer;
    private final File orphansDir;

    public OrphanDiagram(final DiagramProducer diagramProducer, final File orphansDir) {
        this.diagramProducer = diagramProducer;
        this.orphansDir = orphansDir;
    }

    public DiagramResult generateOrphanDiagram(File dotFile, String diagramName) {
        try {
            File diagramFile = new File(orphansDir, diagramName + "." + diagramProducer.getDiagramFormat());
            String diagramMap = diagramProducer.generateDiagram(dotFile, diagramFile);
            return new DiagramResult(diagramFile.getName(), diagramMap, diagramProducer.getDiagramFormat());
        } catch (DiagramException diagramException) {
            throw new DiagramException("Failed to generate Orphan diagram", diagramException);
        }
    }
}