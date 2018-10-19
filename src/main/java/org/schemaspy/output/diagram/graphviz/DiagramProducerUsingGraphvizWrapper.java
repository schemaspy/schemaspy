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
package org.schemaspy.output.diagram.graphviz;

import org.schemaspy.output.diagram.DiagramException;
import org.schemaspy.output.diagram.DiagramProducer;
import org.schemaspy.output.diagram.DiagramResults;
import org.schemaspy.util.Dot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.invoke.MethodHandles;

/**
 * @author Nils Petzaell
 */
public class DiagramProducerUsingGraphvizWrapper implements DiagramProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Dot graphvizWrapper;
    private final File diagramDir;
    private final File summaryDir;
    private final File orphanDir;

    public DiagramProducerUsingGraphvizWrapper(Dot graphvizWrapper, File outputDir) {
        this.graphvizWrapper = graphvizWrapper;
        this.diagramDir = new File(outputDir, "diagrams");
        this.summaryDir = new File(diagramDir, "summary");
        this.orphanDir = new File(diagramDir, "orphans");
        createDirs();
    }

    private void createDirs() {
        diagramDir.mkdirs();
        summaryDir.mkdirs();
        orphanDir.mkdirs();
    }

    @Override
    public String implementationDetails() {
        return "Graphviz dot " + graphvizWrapper.getGraphvizVersion().toString();
    }

    @Override
    public DiagramResults generateOrphanDiagram(File dotFile, String diagramName) {
        try {
            File diagramFile = new File(orphanDir, diagramName + "." + graphvizWrapper.getFormat());
            String diagramMap = graphvizWrapper.generateDiagram(dotFile, diagramFile);
            return new DiagramResults(diagramFile, diagramMapName(diagramMap), diagramMap);
        } catch (Dot.DotFailure dotFailure) {
            throw new DiagramException("Failed to generate Orphan diagram", dotFailure);
        }
    }

    @Override
    public DiagramResults generateTableDiagram(File dotFile, String diagramName) {
        try {
            File diagramFile = new File(diagramDir, diagramName + "." + graphvizWrapper.getFormat());
            String diagramMap = graphvizWrapper.generateDiagram(dotFile, diagramFile);
            return new DiagramResults(diagramFile, diagramMapName(diagramMap), diagramMap);
        } catch (Dot.DotFailure dotFailure) {
            throw new DiagramException("Failed to generate Table diagram", dotFailure);
        }
    }

    @Override
    public DiagramResults generateSummaryDiagram(File dotFile, String diagramName) {
        try {
            File diagramFile = new File(summaryDir, diagramName + "." + graphvizWrapper.getFormat());
            String diagramMap = graphvizWrapper.generateDiagram(dotFile, diagramFile);
            return new DiagramResults(diagramFile, diagramMapName(diagramMap), diagramMap);
        } catch (Dot.DotFailure dotFailure) {
            throw new DiagramException("Failed to generate summary diagram", dotFailure);
        }
    }

    private static String diagramMapName(String diagramMap) {
        BufferedReader reader = new BufferedReader(new StringReader(diagramMap));
        String line;
        String diagramMapName="";
        try {
            line = reader.readLine();
            if (line != null) {
                diagramMapName = line.substring("<map id=\"".length(),line.indexOf("name")-"\" ".length());
            }
        } catch (IOException e) {
            LOGGER.error("Error reading diagram map",e);
        }
        return diagramMapName;
    }
}