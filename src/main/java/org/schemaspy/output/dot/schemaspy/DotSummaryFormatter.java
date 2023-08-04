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

import org.schemaspy.model.Database;
import org.schemaspy.model.Table;
import org.schemaspy.output.dot.RuntimeDotConfig;
import org.schemaspy.output.dot.schemaspy.edge.SimpleEdges;
import org.schemaspy.output.dot.schemaspy.graph.Digraph;
import org.schemaspy.output.dot.schemaspy.graph.Element;
import org.schemaspy.output.dot.schemaspy.name.DefaultName;
import org.schemaspy.output.dot.schemaspy.name.Implied;
import org.schemaspy.output.dot.schemaspy.name.Sized;
import org.schemaspy.util.naming.Concatenation;
import org.schemaspy.util.naming.Name;

import java.io.PrintWriter;
import java.util.*;

/**
 * Format table data into .dot format to feed to Graphvis' dot program.
 *
 * @author John Currier
 * @author Rafal Kasa
 * @author Nils Petzaell
 */
public class DotSummaryFormatter {

    private final RuntimeDotConfig runtimeDotConfig;
    private final boolean includeOrphans;

    public DotSummaryFormatter(RuntimeDotConfig runtimeDotConfig, final boolean includeOrphans) {
        this.runtimeDotConfig = runtimeDotConfig;
        this.includeOrphans = includeOrphans;
    }

    public void writeSummaryRealRelationships(Database db, Collection<Table> tables, boolean compact, PrintWriter dot) {
        writeRelationships(db, tables, compact, false, dot);
    }

    /**
     * Returns <code>true</code> if it wrote any implied relationships
     */
    public void writeSummaryAllRelationships(Database db, Collection<Table> tables, boolean compact, PrintWriter dot) {
        writeRelationships(db, tables, compact, true, dot);
    }

    private void writeRelationships(Database db, Collection<Table> tables, boolean compact, boolean includeImplied, PrintWriter dot) {
        DotNodeConfig nodeConfig = runtimeDotConfig.showDetails(tables) ? new DotNodeConfig(!compact, false) : new DotNodeConfig();

        final Name name = new Concatenation(
                new Sized(compact),
                new Concatenation(
                        new Implied(includeImplied),
                        new DefaultName()
                )
        );


        List<DotNode> nodes = new LinkedList<>();

        for (Table table : tables) {
            if (this.includeOrphans) {
                nodes.add(new DotNode(table, true, nodeConfig, runtimeDotConfig));
            } else {
                if (!table.isOrphan(includeImplied)) {
                    nodes.add(new DotNode(table, true, nodeConfig, runtimeDotConfig));
                }
            }
        }

        for (Table table : db.getRemoteTables()) {
            nodes.add(new DotNode(table, true, nodeConfig, runtimeDotConfig));
        }

        Set<Edge> edges = new TreeSet<>();

        for (DotNode node : nodes) {
            edges.addAll(new SimpleEdges(node.getTable(), includeImplied).unique());
        }

        List<Element> elements = new LinkedList<>();
        elements.addAll(nodes);
        elements.addAll(edges);

        dot.println(
            new Digraph(
                    name,
                    new DotConfigHeader(runtimeDotConfig, true),
                    elements.stream().toArray(Element[]::new)
            ).dot()
        );
        dot.flush();
    }
}
