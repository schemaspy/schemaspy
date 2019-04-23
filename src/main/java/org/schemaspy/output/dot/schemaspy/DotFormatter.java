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

import org.schemaspy.Config;
import org.schemaspy.model.Database;
import org.schemaspy.model.ForeignKeyConstraint;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.schemaspy.output.dot.DotConfig;
import org.schemaspy.output.dot.schemaspy.DotNode.DotNodeConfig;
import org.schemaspy.view.StyleSheet;
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
public class DotFormatter {

    private final DotConfig dotConfig;

    /**
     * Singleton - prevent creation
     */
    public DotFormatter(DotConfig dotConfig) {
        this.dotConfig = dotConfig;
    }

    /**
     * Write real relationships (excluding implied) associated with the given table.<p>
     * Returns a set of the implied constraints that could have been included but weren't.
     */
    public Set<ForeignKeyConstraint> writeRealRelationships(Table table, boolean twoDegreesOfSeparation, WriteStats stats, PrintWriter dot) {
        return writeRelationships(table, twoDegreesOfSeparation, stats, false, dot);
    }

    /**
     * Write implied relationships associated with the given table
     */
    public void writeAllRelationships(Table table, boolean twoDegreesOfSeparation, WriteStats stats, PrintWriter dot) {
        writeRelationships(table, twoDegreesOfSeparation, stats, true, dot);
    }

    /**
     * Write relationships associated with the given table.<p>
     * Returns a set of the implied constraints that could have been included but weren't.
     */
    private Set<ForeignKeyConstraint> writeRelationships(Table table, boolean twoDegreesOfSeparation, WriteStats stats, boolean includeImplied, PrintWriter dot) {
        Set<Table> tablesWritten = new HashSet<>();
        Set<ForeignKeyConstraint> skippedImpliedConstraints = new HashSet<>();

        DotConnectorFinder finder = DotConnectorFinder.getInstance();

        String diagramName = (twoDegreesOfSeparation ? "twoDegreesRelationshipsDiagram" : "oneDegreeRelationshipsDiagram") + (includeImplied ? "Implied" : "");
        writeHeader(diagramName, true, dot);

        Set<Table> relatedTables = getImmediateRelatives(table, true, includeImplied, skippedImpliedConstraints);

        Set<DotConnector> connectors = new TreeSet<>(finder.getRelatedConnectors(table, includeImplied));
        tablesWritten.add(table);

        Map<Table, DotNode> nodes = new TreeMap<>();

        // write immediate relatives first
        for (Table relatedTable : relatedTables) {
            if (!tablesWritten.add(relatedTable))
                continue; // already written

            nodes.put(relatedTable, new DotNode(relatedTable, "", new DotNodeConfig(false, false), dotConfig));
            connectors.addAll(finder.getRelatedConnectors(relatedTable, table, true, includeImplied));
        }

        // connect the edges that go directly to the target table
        // so they go to the target table's type column instead
        for (DotConnector connector : connectors) {
            if (connector.pointsTo(table))
                connector.connectToParentDetails();
        }

        Set<Table> allCousins = new HashSet<>();
        Set<DotConnector> allCousinConnectors = new TreeSet<>();

        // next write 'cousins' (2nd degree of separation)
        if (twoDegreesOfSeparation) {
            for (Table relatedTable : relatedTables) {
                Set<Table> cousins = getImmediateRelatives(relatedTable, false, includeImplied, skippedImpliedConstraints);

                for (Table cousin : cousins) {
                    if (!tablesWritten.add(cousin))
                        continue; // already written

                    allCousinConnectors.addAll(finder.getRelatedConnectors(cousin, relatedTable, false, includeImplied));
                    nodes.put(cousin, new DotNode(cousin, "", new DotNodeConfig(), dotConfig));
                }

                allCousins.addAll(cousins);
            }
        }

        // glue together any 'participants' that aren't yet connected
        // note that this is the epitome of nested loops from hell
        List<Table> participants = new ArrayList<>(nodes.keySet());
        Iterator<Table> iter = participants.iterator();
        while (iter.hasNext()) {
            Table participantA = iter.next();
            iter.remove(); // cut down the combos as quickly as possible

            for (Table participantB : participants) {
                for (DotConnector connector : finder.getRelatedConnectors(participantA, participantB, false, includeImplied)) {
                    if (twoDegreesOfSeparation && (allCousins.contains(participantA) || allCousins.contains(participantB))) {
                        allCousinConnectors.add(connector);
                    } else {
                        connectors.add(connector);
                    }
                }
            }
        }

        markExcludedColumns(nodes, stats.getExcludedColumns());

        // now directly connect the loose ends to the title of the
        // 2nd degree of separation tables
        for (DotConnector connector : allCousinConnectors) {
            if (allCousins.contains(connector.getParentTable()) && !relatedTables.contains(connector.getParentTable()))
                connector.connectToParentTitle();
            if (allCousins.contains(connector.getChildTable()) && !relatedTables.contains(connector.getChildTable()))
                connector.connectToChildTitle();
        }

        // include the table itself
        nodes.put(table, new DotNode(table, "", new DotNodeConfig(true, true), dotConfig));

        connectors.addAll(allCousinConnectors);
        for (DotConnector connector : connectors) {
            if (connector.isImplied()) {
                DotNode node = nodes.get(connector.getParentTable());
                if (node != null)
                    node.setShowImplied(true);
                node = nodes.get(connector.getChildTable());
                if (node != null)
                    node.setShowImplied(true);
            }
            dot.println(connector.toString());
        }

        for (DotNode node : nodes.values()) {
            dot.println(node.toString());
            stats.wroteTable(node.getTable());
        }

        dot.println("}");

        return skippedImpliedConstraints;
    }

    private static Set<Table> getImmediateRelatives(Table table, boolean includeExcluded, boolean includeImplied, Set<ForeignKeyConstraint> skippedImpliedConstraints) {
        Set<TableColumn> relatedColumns = new HashSet<>();

        for (TableColumn column : table.getColumns()) {
            if (column.isAllExcluded() || (!includeExcluded && column.isExcluded())) {
                continue;
            }

            for (TableColumn childColumn : column.getChildren()) {
                if (childColumn.isAllExcluded() || (!includeExcluded && childColumn.isExcluded())) {
                    continue;
                }

                ForeignKeyConstraint constraint = column.getChildConstraint(childColumn);
                if (includeImplied || !constraint.isImplied())
                    relatedColumns.add(childColumn);
                else
                    skippedImpliedConstraints.add(constraint);
            }

            for (TableColumn parentColumn : column.getParents()) {
                if (parentColumn.isAllExcluded() || (!includeExcluded && parentColumn.isExcluded())) {
                    continue;
                }

                ForeignKeyConstraint constraint = column.getParentConstraint(parentColumn);
                if (includeImplied || !constraint.isImplied())
                    relatedColumns.add(parentColumn);
                else
                    skippedImpliedConstraints.add(constraint);
            }
        }

        Set<Table> relatedTables = new HashSet<>();
        for (TableColumn column : relatedColumns)
            relatedTables.add(column.getTable());

        relatedTables.remove(table);

        return relatedTables;
    }

    private void writeHeader(String diagramName, boolean showLabel, PrintWriter dot) {
        dot.println("digraph \"" + diagramName + "\" {");
        dot.println("  graph [");
        boolean rankdirbug = Config.getInstance().isRankDirBugEnabled();
        if (!rankdirbug)
            dot.println("    rankdir=\"RL\"");
        dot.println("    bgcolor=\"" + StyleSheet.getInstance().getBodyBackground() + "\"");
        if (showLabel) {
            if (rankdirbug)
                dot.println("    label=\"\\nLayout is significantly better without '-rankdirbug' option\"");
            else
                dot.println("    label=\"\\nGenerated by SchemaSpy\"");
            dot.println("    labeljust=\"l\"");
        }
        dot.println("    nodesep=\"0.18\"");
        dot.println("    ranksep=\"0.46\"");
        dot.println("    fontname=\"" + Config.getInstance().getFont() + "\"");
        dot.println("    fontsize=\"" + dotConfig.getFontSize() + "\"");
        dot.println("    ration=\"compress\"");
        dot.println("  ];");
        dot.println("  node [");
        dot.println("    fontname=\"" + Config.getInstance().getFont() + "\"");
        dot.println("    fontsize=\"" + dotConfig.getFontSize() + "\"");
        dot.println("    shape=\"plaintext\"");
        dot.println("  ];");
        dot.println("  edge [");
        dot.println("    arrowsize=\"0.8\"");
        dot.println("  ];");
}

    public void writeRealRelationships(Database db, Collection<Table> tables, boolean compact, boolean showColumns, WriteStats stats, PrintWriter dot) {
        writeRelationships(db, tables, compact, showColumns, false, stats, dot);
    }

    /**
     * Returns <code>true</code> if it wrote any implied relationships
     */
    public boolean writeAllRelationships(Database db, Collection<Table> tables, boolean compact, boolean showColumns, WriteStats stats, PrintWriter dot) {
        return writeRelationships(db, tables, compact, showColumns, true, stats, dot);
    }

    private boolean writeRelationships(Database db, Collection<Table> tables, boolean compact, boolean showColumns, boolean includeImplied, WriteStats stats, PrintWriter dot) {
        DotConnectorFinder finder = DotConnectorFinder.getInstance();
        DotNodeConfig nodeConfig = showColumns ? new DotNodeConfig(!compact, false) : new DotNodeConfig();
        boolean wroteImplied = false;

        String diagramName;
        if (includeImplied) {
            if (compact)
                diagramName = "compactImpliedRelationshipsDiagram";
            else
                diagramName = "largeImpliedRelationshipsDiagram";
        } else {
            if (compact)
                diagramName = "compactRelationshipsDiagram";
            else
                diagramName = "largeRelationshipsDiagram";
        }
        writeHeader(diagramName, true, dot);

        Map<Table, DotNode> nodes = new TreeMap<>();

        for (Table table : tables) {
            if (!table.isOrphan(includeImplied)) {
                nodes.put(table, new DotNode(table, "tables/", nodeConfig, dotConfig));
            }
        }

        for (Table table : db.getRemoteTables()) {
            nodes.put(table, new DotNode(table, "tables/", nodeConfig, dotConfig));
        }

        Set<DotConnector> connectors = new TreeSet<>();

        for (DotNode node : nodes.values()) {
            connectors.addAll(finder.getRelatedConnectors(node.getTable(), includeImplied));
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

    public void writeOrphan(Table table, PrintWriter dot) {
        writeHeader(table.getName(), false, dot);
        DotNodeConfig nodeConfig = new DotNodeConfig(true, true);
        dot.println(new DotNode(table, "tables/", nodeConfig, dotConfig).toString());
        dot.println("}");
        dot.flush();
    }
}
