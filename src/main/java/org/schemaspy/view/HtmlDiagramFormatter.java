/*
 * Copyright (C) 2004 - 2010 John Currier
 * Copyright (C) 2016 Rafal Kasa
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

import org.schemaspy.util.Dot;

/**
 * @author John Currier
 * @author Rafal Kasa
 */
public class HtmlDiagramFormatter extends HtmlFormatter {
    private static boolean printedNoDotWarning = false;
    private static boolean printedInvalidVersionWarning = false;

    protected HtmlDiagramFormatter() {
    }

    protected Dot getDot() {
        Dot dot = Dot.getInstance();
        if (!dot.exists()) {
            if (!printedNoDotWarning) {
                printedNoDotWarning = true;
                System.err.println();
                System.err.println("Warning: Failed to run dot.");
                System.err.println("   Download " + dot.getSupportedVersions());
                System.err.println("   from www.graphviz.org and make sure that dot is either in your path");
                System.err.println("   or point to where you installed Graphviz with the -gv option.");
                System.err.println("   Generated pages will not contain a diagramtic view of table relationships.");
            }

            return null;
        }

        if (!dot.isValid()) {
            if (!printedInvalidVersionWarning) {
                printedInvalidVersionWarning = true;
                System.err.println();
                System.err.println("Warning: Invalid version of Graphviz dot detected (" + dot.getGraphvizVersion() + ").");
                System.err.println("   SchemaSpy requires " + dot.getSupportedVersions() + ". from www.graphviz.org.");
                System.err.println("   Generated pages will not contain a diagramatic view of table relationships.");
            }

            return null;
        }

        return dot;
    }
}
