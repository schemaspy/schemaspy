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
package org.schemaspy.output.html.mustache.diagrams;

import org.schemaspy.output.diagram.DiagramProducer;
import org.schemaspy.output.diagram.DiagramResults;
import org.schemaspy.view.MustacheTableDiagram;

import java.io.File;

public class MustacheDiagramFactory {

    private final DiagramProducer diagramProducer;

    public MustacheDiagramFactory(DiagramProducer diagramProducer) {
        this.diagramProducer = diagramProducer;
    }

    public MustacheTableDiagram generateOrphanDiagram(String name, File dotFile, String diagramName) {
        DiagramResults results = diagramProducer.generateOrphanDiagram(dotFile, diagramName);
        return createMustacheTableDiagram(name, results);
    }

    public MustacheTableDiagram generateTableDiagram(String name, File dotFile, String diagramName) {
        DiagramResults results = diagramProducer.generateTableDiagram(dotFile, diagramName);
        return createMustacheTableDiagram(name, results);
    }

    public MustacheTableDiagram generateSummaryDiagram(String name, File dotFile, String diagramName) {
        DiagramResults results = diagramProducer.generateSummaryDiagram(dotFile, diagramName);
        return createMustacheTableDiagram(name, results);
    }

    private static MustacheTableDiagram createMustacheTableDiagram(String diagramName, DiagramResults diagramResults) {
        MustacheTableDiagram mustacheTableDiagram = new MustacheTableDiagram();
        mustacheTableDiagram.setName(diagramName);
        mustacheTableDiagram.setId(diagramName.replaceAll("\\s", "").toLowerCase() + "DegreeImg");
        mustacheTableDiagram.setFileName(diagramResults.getDiagramFile().getName());
        mustacheTableDiagram.setMap(diagramResults.getDiagramMap());
        mustacheTableDiagram.setMapName(diagramResults.getDiagramMapName());
        return mustacheTableDiagram;
    }

}