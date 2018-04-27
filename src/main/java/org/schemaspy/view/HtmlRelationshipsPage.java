/*
 * Copyright (C) 2004 - 2011 John Currier
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2016 Ismail Simsek
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

import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.model.TableColumn;
import org.schemaspy.util.DiagramUtil;
import org.schemaspy.util.Dot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * The page that contains the overview entity relationship diagrams.
 *
 * @author John Currier
 * @author Rafal Kasa
 * @author Ismail Simsek
 */
public class HtmlRelationshipsPage extends HtmlDiagramFormatter {
    private static final HtmlRelationshipsPage instance = new HtmlRelationshipsPage();

    /**
     * Singleton: Don't allow instantiation
     */
    private HtmlRelationshipsPage() {
    }

    /**
     * Singleton accessor
     *
     * @return the singleton instance
     */
    public static HtmlRelationshipsPage getInstance() {
        return instance;
    }

    public boolean write(Database db, File diagramDir, String dotBaseFilespec, boolean hasRealRelationships, boolean hasImpliedRelationships,
    					Set<TableColumn> excludedColumns, ProgressListener listener, File outputDir) {

        try {
            Dot dot = getDot();
            Object graphvizExists = dot;

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
            	listener.graphingSummaryProgressed();
                DiagramUtil.generateDiagram("Compact", dot, compactRelationshipsDotFile, compactRelationshipsDiagramFile, diagrams, false, false);

                // we've run into instances where the first diagrams get generated, but then
                // dot fails on the second one...try to recover from that scenario 'somewhat'
                // gracefully
                try {
                	listener.graphingSummaryProgressed();
                    DiagramUtil.generateDiagram("Large", dot, largeRelationshipsDotFile, largeRelationshipsDiagramFile, diagrams, false, false);
                } catch (Dot.DotFailure dotFailure) {
                    System.err.println("dot failed to generate all of the relationships diagrams:");
                    System.err.println(dotFailure);
                    System.err.println("...but the relationships page may still be usable.");
                }
            }

            try {
                if (hasImpliedRelationships) {
                	listener.graphingSummaryProgressed();
                    DiagramUtil.generateDiagram("Compact Implied", dot, compactImpliedDotFile, compactImpliedDiagramFile, diagrams, false, true);

                	listener.graphingSummaryProgressed();
                    DiagramUtil.generateDiagram("Large Implied", dot, largeImpliedDotFile, largeImpliedDiagramFile, diagrams, false, true);
                }
            } catch (Dot.DotFailure dotFailure) {
                System.err.println("dot failed to generate all of the relationships diagrams:");
                System.err.println(dotFailure);
                System.err.println("...but the relationships page may still be usable.");
            }

        	listener.graphingSummaryProgressed();

            //writeExcludedColumns(excludedColumns, null, html);

            DiagramUtil.markFirstAsActive(diagrams);

            HashMap<String, Object> scopes = new HashMap<String, Object>();
            String graphvizVersion = Dot.getInstance().getSupportedVersions().substring(4);
            scopes.put("graphvizExists", graphvizExists);
            scopes.put("graphvizVersion", graphvizVersion);
            scopes.put("diagramExists", DiagramUtil.diagramExists(diagrams));
            scopes.put("hasOnlyImpliedRelationships", hasOnlyImpliedRelationships(hasRealRelationships, hasImpliedRelationships));
            scopes.put("anyRelationships", anyRelationships(hasRealRelationships, hasImpliedRelationships));
            scopes.put("diagrams", diagrams);

            MustacheWriter mw = new MustacheWriter(outputDir, scopes, getPathToRoot(), db.getName(), false);
            mw.write("relationships.html", "relationships.html", "relationships.js");
            return true;
        } catch (Dot.DotFailure dotFailure) {
            System.err.println(dotFailure);
            return false;
        } catch (IOException ioExc) {
            ioExc.printStackTrace();
            return false;
        }
    }

    private Object hasOnlyImpliedRelationships(boolean hasRealRelationships, boolean hasImpliedRelationships) {
        return !hasRealRelationships && hasImpliedRelationships ? new Object() : null;
    }

    private Object anyRelationships(boolean hasRealRelationships, boolean hasImpliedRelationships) {
        return !hasRealRelationships && !hasImpliedRelationships ? new Object() : null;
    }
}