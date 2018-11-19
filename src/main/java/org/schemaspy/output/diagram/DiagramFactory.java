/*
 * Copyright (C) 2018 Nils Petzaell
 *
 * This file is part of SchemaSpy.
 *
 * SchemaSpy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SchemaSpy. If not, see <http://www.gnu.org/licenses/>.
 */
package org.schemaspy.output.diagram;

import java.io.File;

public class DiagramFactory {

    private final DiagramProducer diagramProducer;
    private final File diagramDir;
    private final File tablesDir;
    private final File summaryDir;
    private final File orphansDir;

    public DiagramFactory(DiagramProducer diagramProducer, File outputDir) {
        this.diagramProducer = diagramProducer;
        this.diagramDir = new File(outputDir, "diagrams");
        this.tablesDir = new File(diagramDir, "tables");
        this.summaryDir = new File(diagramDir, "summary");
        this.orphansDir = new File(diagramDir, "orphans");
        createDirs();
    }

    private void createDirs() {
        diagramDir.mkdirs();
        tablesDir.mkdirs();
        summaryDir.mkdirs();
        orphansDir.mkdirs();
    }

    public String getImplementationDetails() {
        return diagramProducer.getImplementationDetails();
    }

    public DiagramResults generateOrphanDiagram(File dotFile, String diagramName) {
        try {
            File diagramFile = new File(orphansDir, diagramName + "." + diagramProducer.getDiagramFormat());
            String diagramMap = diagramProducer.generateDiagram(dotFile, diagramFile);
            return new DiagramResults(diagramFile, diagramMap, diagramProducer.getDiagramFormat());
        } catch (DiagramException diagramException) {
            throw new DiagramException("Failed to generate Orphan diagram", diagramException);
        }
    }

    public DiagramResults generateTableDiagram(File dotFile, String diagramName) {
        try {
            File diagramFile = new File(tablesDir, diagramName + "." + diagramProducer.getDiagramFormat());
            String diagramMap = diagramProducer.generateDiagram(dotFile, diagramFile);
            return new DiagramResults(diagramFile, diagramMap, diagramProducer.getDiagramFormat());
        } catch (DiagramException diagramException) {
            throw new DiagramException("Failed to generate Table diagram", diagramException);
        }
    }

    public DiagramResults generateSummaryDiagram(File dotFile, String diagramName) {
        try {
            File diagramFile = new File(summaryDir, diagramName + "." + diagramProducer.getDiagramFormat());
            String diagramMap = diagramProducer.generateDiagram(dotFile, diagramFile);
            return new DiagramResults(diagramFile, diagramMap, diagramProducer.getDiagramFormat());
        } catch (DiagramException diagramException) {
            throw new DiagramException("Failed to generate summary diagram", diagramException);
        }
    }
}
