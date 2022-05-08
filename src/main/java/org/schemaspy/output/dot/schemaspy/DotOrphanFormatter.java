/*
 * Copyright (C) 2004 - 2011 John Currier
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
package org.schemaspy.output.dot.schemaspy;

import org.schemaspy.output.dot.schemaspy.name.Name;

import java.io.PrintWriter;

/**
 * Format table data into .dot format to feed to Graphvis' dot program.
 *
 * @author John Currier
 * @author Rafal Kasa
 * @author Nils Petzaell
 */
public class DotOrphanFormatter {

    private final PrintWriter dot;
    private final Name name;
    private final Header header;
    private final Node node;

    public DotOrphanFormatter(final PrintWriter dot, final Name name, final Header header, final Node node) {
        this.dot = dot;
        this.name = name;
        this.header = header;
        this.node = node;
    }

    public void writeOrphan() {
        this.dot.println("digraph \"" + name.value() + "\" {");
        this.dot.println(this.header.value());
        this.dot.println(this.node.value());
        this.dot.println("}");
        this.dot.flush();
    }
}
