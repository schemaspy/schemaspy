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

import org.schemaspy.output.diagram.DiagramException;
import org.schemaspy.output.diagram.Renderer;
import org.schemaspy.output.dot.schemaspy.graph.Graph;
import org.schemaspy.output.html.HtmlException;
import org.schemaspy.output.html.mustache.Diagram;
import org.schemaspy.output.html.mustache.ImgDiagram;
import org.schemaspy.output.html.mustache.SvgDiagram;
import org.schemaspy.util.Writers;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;

/**
 * Generates Diagrams for Orphans
 *
 * @author Nils Petzaell
 */
public class OrphanDiagram implements Diagram {

    private static final String NAME = "orphans";
    private final Graph graph;
    private final Renderer renderer;
    private final Path outputDir;

    public OrphanDiagram(Graph graph, Renderer renderer, File outputDir) {
        this.graph = graph;
        this.renderer = renderer;
        this.outputDir = outputDir.toPath().resolve("diagrams").resolve(NAME);
    }

    @Override
    public String html() {
        outputDir.toFile().mkdirs();
        return writeDiagram(writeDot()).html();
    }

    private Path writeDot() {
        Path dotFile = outputDir.resolve(fileName("dot"));
        try (PrintWriter dotOut = Writers.newPrintWriter(dotFile.toFile())) {
            dotOut.println(graph.dot());
            dotOut.flush();
        } catch (IOException e) {
            throw new HtmlException("Failed to write dot: " + dotFile, e);
        }
        return dotFile;
    }

    private Diagram writeDiagram(Path dotFile) {
        try {
            Path diagramFile = outputDir.resolve(fileName(renderer.format()));
            String diagramMap = renderer.render(dotFile.toFile(), diagramFile.toFile());
            String diagramSource = "diagrams/orphans/" + diagramFile.getFileName().toString();
            if ("svg".equalsIgnoreCase(renderer.format())) {
                return new SvgDiagram(NAME, diagramSource);
            }
            return new ImgDiagram(NAME, diagramSource, diagramMap);
        } catch (DiagramException diagramException) {
            throw new HtmlException("Failed to generate Orphan diagram", diagramException);
        }
    }

    private String fileName(String extension) {
        return NAME + "." + extension;
    }
}
