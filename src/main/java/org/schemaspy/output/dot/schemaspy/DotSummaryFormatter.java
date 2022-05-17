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
import org.schemaspy.model.TableColumn;
import org.schemaspy.output.dot.DotConfig;
import org.schemaspy.output.dot.schemaspy.edge.SimpleEdges;
import org.schemaspy.output.dot.schemaspy.graph.Element;
import org.schemaspy.output.dot.schemaspy.name.*;
import org.schemaspy.view.WriteStats;

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

    private final DotConfig dotConfig;

    public DotSummaryFormatter(DotConfig dotConfig) {
        this.dotConfig = dotConfig;
    }

    public void writeSummaryRealRelationships(Database db, Collection<Table> tables, boolean compact, boolean showColumns, WriteStats stats, PrintWriter dot) {
        writeRelationships(db, tables, compact, showColumns, false, stats, dot);
    }

    /**
     * Returns <code>true</code> if it wrote any implied relationships
     */
    public boolean writeSummaryAllRelationships(Database db, Collection<Table> tables, boolean compact, boolean showColumns, WriteStats stats, PrintWriter dot) {
        return writeRelationships(db, tables, compact, showColumns, true, stats, dot);
    }

    private boolean writeRelationships(Database db, Collection<Table> tables, boolean compact, boolean showColumns, boolean includeImplied, WriteStats stats, PrintWriter dot) {
        DotNodeConfig nodeConfig = showColumns ? new DotNodeConfig(!compact, false) : new DotNodeConfig();
        boolean wroteImplied = false;

        final Name name = new Concatenation(
                new Sized(compact),
                new Concatenation(
                        new Implied(includeImplied),
                        new DefaultName()
                )
        );


        Map<Table, DotNode> nodes = new TreeMap<>();

        for (Table table : tables) {
            if (!table.isOrphan(includeImplied)) {
                nodes.put(table, new DotNode(table, true, nodeConfig, dotConfig));
            }
        }

        for (Table table : db.getRemoteTables()) {
            nodes.put(table, new DotNode(table, true, nodeConfig, dotConfig));
        }

        Set<Edge> edges = new TreeSet<>();

        for (DotNode node : nodes.values()) {
            edges.addAll(new SimpleEdges(node.getTable(), includeImplied).unique());
        }

        markExcludedColumns(nodes, stats.getExcludedColumns());

        List<Element> elements = new LinkedList<>();

        for (DotNode node : nodes.values()) {
            Table table = node.getTable();

            elements.add(node);
            stats.wroteTable(table);
            wroteImplied = wroteImplied || (includeImplied && table.isOrphan(false));
        }

        elements.addAll(edges);

        dot.println("digraph \"" + name.value() + "\" {");
        Header header = new DotConfigHeader(dotConfig, true);
        dot.println(header.value());
        for (Element element: elements) {
            dot.println(element.value());
        }
        dot.println("}");
        dot.flush();
        return wroteImplied;
    }

    private static void markExcludedColumns(Map<Table, DotNode> nodes, Set<TableColumn> excludedColumns) {
        for (TableColumn column : excludedColumns) {
            DotNode node = nodes.get(column.getTable());
            if (node != null) {
                node.excludeColumn(column);
            }
        }
    }
}
