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
import org.schemaspy.output.html.mustache.diagrams.MustacheSummaryDiagramResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.lang.invoke.MethodHandles;

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
            MustacheSummaryDiagramResults results,
            Writer writer
    ) {
        try {
            PageData pageData = new PageData.Builder()
                    .templateName("relationships.html")
                    .scriptName("relationships.js")
                    .addToScope("hasOnlyImpliedRelationships", hasOnlyImpliedRelationships(results))
                    .addToScope("anyRelationships", anyRelationships(results))
                    .addToScope("diagrams", results.getDiagrams())
                    .addToScope("diagramErrors", results.getOutputExceptions())
                    .depth(0)
                    .getPageData();

            mustacheCompiler.write(pageData, writer);
            return true;
        } catch (IOException ioExc) {
            LOGGER.error("Error occurred during generation of relationships", ioExc);
            return false;
        }
    }

    private static boolean hasOnlyImpliedRelationships(MustacheSummaryDiagramResults results) {
        return !results.hasRealRelationships() && !results.getImpliedConstraints().isEmpty() ? true : false;
    }

    private static boolean anyRelationships(MustacheSummaryDiagramResults results) {
        return !results.hasRealRelationships() && results.getImpliedConstraints().isEmpty() ? true : false;
    }
}