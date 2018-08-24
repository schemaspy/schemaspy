/*
 * Copyright (C) 2004 - 2011 John Currier
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2016 Ismail Simsek
 * Copyright (C) 2018 Nils Petzaell
 *
 * This file is a part of the SchemaSpy project (http://schemaspy.org).
 *
 * SchemaSpy is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.schemaspy.view;

import org.schemaspy.model.ProgressListener;
import org.schemaspy.util.DiagramUtil;
import org.schemaspy.util.Dot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

/**
 * The page that contains the overview entity relationship diagrams.
 *
 * @author John Currier
 * @author Rafal Kasa
 * @author Ismail Simsek
 * @author Nils Petzaell
 */
public class HtmlRelationshipsPage extends HtmlDiagramFormatter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final MustacheCompiler mustacheCompiler;

    public HtmlRelationshipsPage(MustacheCompiler mustacheCompiler) {
        this.mustacheCompiler = mustacheCompiler;
    }

    public boolean write(
            File diagramDir,
            String dotBaseFilespec,
            boolean hasRealRelationships,
            boolean hasImpliedRelationships,
            ProgressListener listener,
            Writer writer
    ) {
        try {
            Dot dot = getDot();

            if (dot == null) //if null mean that it was problem with dot Graphviz initialization
                return false;

            File compactRelationshipsDotFile = new File(diagramDir, dotBaseFilespec + ".real.compact.dot");
            File compactRelationshipsDiagramFile = new File(diagramDir, dotBaseFilespec + ".real.compact." + dot.getFormat());
            File largeRelationshipsDotFile = new File(diagramDir, dotBaseFilespec + ".real.large.dot");
            File largeRelationshipsDiagramFile = new File(diagramDir, dotBaseFilespec + ".real.large." + dot.getFormat());
            File compactImpliedDotFile = new File(diagramDir, dotBaseFilespec + ".implied.compact.dot");
            File compactImpliedDiagramFile = new File(diagramDir, dotBaseFilespec + ".implied.compact." + dot.getFormat());
            File largeImpliedDotFile = new File(diagramDir, dotBaseFilespec + ".implied.large.dot");
            File largeImpliedDiagramFile = new File(diagramDir, dotBaseFilespec + ".implied.large." + dot.getFormat());

            List<MustacheTableDiagram> diagrams = new ArrayList<>();

            if (hasRealRelationships) {
                generateRelationshipDiagrams(listener, dot, compactRelationshipsDotFile, compactRelationshipsDiagramFile, largeRelationshipsDotFile, largeRelationshipsDiagramFile, diagrams);
            }

            if (hasImpliedRelationships) {
                generateImpliedRelationshipDiagrams(listener, dot, compactImpliedDotFile, compactImpliedDiagramFile, largeImpliedDotFile, largeImpliedDiagramFile, diagrams);
            }

            listener.graphingSummaryProgressed();

            DiagramUtil.markFirstAsActive(diagrams);

            String graphvizVersion = Dot.getInstance().getSupportedVersions().substring(4);
            Object graphvizExists = dot;

            PageData pageData = new PageData.Builder()
                    .templateName("relationships.html")
                    .scriptName("relationships.js")
                    .addToScope("graphvizExists", graphvizExists)
                    .addToScope("graphvizVersion", graphvizVersion)
                    .addToScope("diagramExists", DiagramUtil.diagramExists(diagrams))
                    .addToScope("hasOnlyImpliedRelationships", hasOnlyImpliedRelationships(hasRealRelationships, hasImpliedRelationships))
                    .addToScope("anyRelationships", anyRelationships(hasRealRelationships, hasImpliedRelationships))
                    .addToScope("diagrams", diagrams)
                    .depth(0)
                    .getPageData();

            mustacheCompiler.write(pageData, writer);
            return true;
        } catch (IOException ioExc) {
            LOGGER.error("Error occurred during generation of relationships", ioExc);
            return false;
        }
    }

    private static void generateRelationshipDiagrams(ProgressListener listener, Dot dot, File compactRelationshipsDotFile, File compactRelationshipsDiagramFile, File largeRelationshipsDotFile, File largeRelationshipsDiagramFile, List<MustacheTableDiagram> diagrams) throws IOException {
        try {
            listener.graphingSummaryProgressed();
            DiagramUtil.generateDiagram("Compact", dot, compactRelationshipsDotFile, compactRelationshipsDiagramFile, diagrams, false, false);
        } catch (Dot.DotFailure dotFailure) {
            LOGGER.error("Failed to generate compact relationship diagram", dotFailure);
        }

        try {
            listener.graphingSummaryProgressed();
            DiagramUtil.generateDiagram("Large", dot, largeRelationshipsDotFile, largeRelationshipsDiagramFile, diagrams, false, false);
        } catch (Dot.DotFailure dotFailure) {
            LOGGER.error("Failed to generate large relationship diagram", dotFailure);
        }
    }

    private static void generateImpliedRelationshipDiagrams(ProgressListener listener, Dot dot, File compactImpliedDotFile, File compactImpliedDiagramFile, File largeImpliedDotFile, File largeImpliedDiagramFile, List<MustacheTableDiagram> diagrams) throws IOException {
        try {
            listener.graphingSummaryProgressed();
            DiagramUtil.generateDiagram("Compact Implied", dot, compactImpliedDotFile, compactImpliedDiagramFile, diagrams, false, true);
        } catch (Dot.DotFailure dotFailure) {
            LOGGER.error("Failed to generate compact implied relationship diagram", dotFailure);
        }
        try {
            listener.graphingSummaryProgressed();
            DiagramUtil.generateDiagram("Large Implied", dot, largeImpliedDotFile, largeImpliedDiagramFile, diagrams, false, true);
        } catch (Dot.DotFailure dotFailure) {
            LOGGER.error("Failed to generate large implied relationship diagram", dotFailure);
        }
    }

    private static Object hasOnlyImpliedRelationships(boolean hasRealRelationships, boolean hasImpliedRelationships) {
        return !hasRealRelationships && hasImpliedRelationships ? new Object() : null;
    }

    private static Object anyRelationships(boolean hasRealRelationships, boolean hasImpliedRelationships) {
        return !hasRealRelationships && !hasImpliedRelationships ? new Object() : null;
    }
}