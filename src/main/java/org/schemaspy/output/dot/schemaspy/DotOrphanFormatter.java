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

import org.schemaspy.model.Table;
import org.schemaspy.output.dot.DotConfig;

import java.io.PrintWriter;

/**
 * Format table data into .dot format to feed to Graphvis' dot program.
 *
 * @author John Currier
 * @author Rafal Kasa
 * @author Nils Petzaell
 */
public class DotOrphanFormatter {

    private final DotConfig dotConfig;

    public DotOrphanFormatter(DotConfig dotConfig) {
        this.dotConfig = dotConfig;
    }

    public void writeOrphan(Table table, PrintWriter dot) {
        Header header = new DotConfigHeader(dotConfig, table.getName(), false);
        dot.println(header.value());
        DotNodeConfig nodeConfig = new DotNodeConfig(true, true);
        dot.println(new DotNode(table, true, nodeConfig, dotConfig).toString());
        dot.println("}");
        dot.flush();
    }
}
