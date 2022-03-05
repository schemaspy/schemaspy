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
import org.schemaspy.output.dot.schemaspy.connectors.SimpleConnectors;
import org.schemaspy.output.dot.schemaspy.name.DefaultName;
import org.schemaspy.output.dot.schemaspy.name.EmptyName;
import org.schemaspy.output.dot.schemaspy.name.Implied;
import org.schemaspy.output.dot.schemaspy.name.Sized;
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

        String diagramName = new Sized(
                compact,
                new Implied(
                        includeImplied,
                        new DefaultName(
                                new EmptyName()
                        )
                )
        ).value();
        DotFormat format = new DotFormat(dotConfig, diagramName, true);
        dot.println(format.header());

        Map<Table, DotNode> nodes = new TreeMap<>();

        for (Table table : tables) {
            if (!table.isOrphan(includeImplied)) {
                nodes.put(table, new DotNode(table, true, nodeConfig, dotConfig));
            }
        }

        for (Table table : db.getRemoteTables()) {
            nodes.put(table, new DotNode(table, true, nodeConfig, dotConfig));
        }

        Set<DotConnector> connectors = new TreeSet<>();

        for (DotNode node : nodes.values()) {
            connectors.addAll(new SimpleConnectors(node.getTable(), includeImplied).unique());
        }

        markExcludedColumns(nodes, stats.getExcludedColumns());

        for (DotNode node : nodes.values()) {
            Table table = node.getTable();

            dot.println(node.toString());
            stats.wroteTable(table);
            wroteImplied = wroteImplied || (includeImplied && table.isOrphan(false));
        }

        for (DotConnector connector : connectors) {
            dot.println(connector.toString());
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
