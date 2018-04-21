/*
 * This file is a part of the SchemaSpy project (http://schemaspy.org).
 * Copyright (C) 2004, 2005, 2006, 2007, 2008, 2009, 2010 John Currier
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class HtmlDiagramFormatter extends HtmlFormatter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static boolean printedNoDotWarning = false;
    private static boolean printedInvalidVersionWarning = false;

    protected HtmlDiagramFormatter() {
    }

    protected static Dot getDot() {
        Dot dot = Dot.getInstance();
        if (!dot.exists()) {
            if (!printedNoDotWarning) {
                printedNoDotWarning = true;
                LOGGER.warn("Failed to run dot, generated pages will not contain relationship diagrams. Make sure dot executable is in your path or specify location using '-gv' on commandline, if graphviz is not installed, download and install version greater than 2.31.");
            }

            return null;
        }

        if (!dot.isValid()) {
            if (!printedInvalidVersionWarning) {
                printedInvalidVersionWarning = true;
                LOGGER.warn("Incompatible version of graphviz detected, download and install graphviz version greater than 2.31");
            }

            return null;
        }

        return dot;
    }
}
