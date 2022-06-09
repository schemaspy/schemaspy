/*
 * Copyright (C) 2004 - 2011 John Currier
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2016 Ismail Simsek
 * Copyright (C) 2017 Wojciech Kasa
 * Copyright (C) 2017 Daniel Watt
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

import org.schemaspy.output.html.HtmlException;
import org.schemaspy.output.html.mustache.DiagramElement;

import java.io.IOException;
import java.io.Writer;

/**
 * The page that contains the all tables that aren't related to others (orphans)
 *
 * @author John Currier
 * @author Rafal Kasa
 * @author Ismail Simsek
 * @author Wojciech Kasa
 * @author Daniel Watt
 * @author Nils Petzaell
 */
public class HtmlOrphansPage {

    private final MustacheCompiler mustacheCompiler;
    private final DiagramElement diagramElement;

    public HtmlOrphansPage(
            MustacheCompiler mustacheCompiler,
            DiagramElement diagramElement
    ) {
        this.mustacheCompiler = mustacheCompiler;
        this.diagramElement = diagramElement;
    }

    public void write(Writer writer) {
        PageData pageData = createPageData();
        try {
            mustacheCompiler.write(pageData, writer);
        } catch (IOException e) {
            throw new HtmlException("Failed to write orphans page", e);
        }
    }

    private PageData createPageData() {
        return new PageData.Builder()
            .templateName("orphans.html")
            .addToScope("diagram", diagramElement.html())
            .getPageData();
    }
}
