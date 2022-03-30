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

import org.schemaspy.model.ForeignKeyConstraint;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.schemaspy.output.dot.DotConfig;
import org.schemaspy.output.dot.schemaspy.columnsfilter.factory.Default;
import org.schemaspy.output.dot.schemaspy.columnsfilter.factory.Factory;
import org.schemaspy.output.dot.schemaspy.columnsfilter.factory.Included;
import org.schemaspy.output.dot.schemaspy.connectors.PairConnectors;
import org.schemaspy.output.dot.schemaspy.connectors.SimpleConnectors;
import org.schemaspy.output.dot.schemaspy.name.*;
import org.schemaspy.output.dot.schemaspy.relationship.Relationships;
import org.schemaspy.output.dot.schemaspy.columnsfilter.Columns;
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
public class DotTableFormatter implements Relationships {

    private final DotConfig dotConfig;
    private final Table table;
    private final boolean twoDegreesOfSeparation;
    private final WriteStats stats;
    private final boolean includeImplied;
    private final PrintWriter dot;
    private final Header header;

    public DotTableFormatter(
            final DotConfig dotConfig,
            final Table table,
            final boolean twoDegreesOfSeparation,
            final WriteStats stats,
            final boolean includeImplied,
            final PrintWriter dot
    ) {
        this(
                dotConfig,
                table,
                twoDegreesOfSeparation,
                stats,
                includeImplied,
                dot,
                new DotConfigHeader(
                        dotConfig,
                        new Concatenation(
                                new Degree(twoDegreesOfSeparation),
                                new DefaultName(
                                        new Implied(
                                                includeImplied,
                                                new EmptyName()
                                        )
                                )
                        ),
                        true
                )
        );
    }

    public DotTableFormatter(
        final DotConfig dotConfig,
        final Table table,
        final boolean twoDegreesOfSeparation,
        final WriteStats stats,
        final boolean includeImplied,
        final PrintWriter dot,
        final Header header
    ) {
        this.dotConfig = dotConfig;
        this.table = table;
        this.twoDegreesOfSeparation = twoDegreesOfSeparation;
        this.stats = stats;
        this.includeImplied = includeImplied;
        this.dot = dot;
        this.header = header;
    }

    @Override
    public Set<ForeignKeyConstraint> write() {
        return writeTableRelationships();
    }

    /**
     * Write relationships associated with the given table.<p>
     * Returns a set of the implied constraints that could have been included but weren't.
     */
    private Set<ForeignKeyConstraint> writeTableRelationships() {
        Set<Table> tablesWritten = new HashSet<>();
        Set<ForeignKeyConstraint> skippedImpliedConstraints = new HashSet<>();

        dot.println(header.value());

        Factory factory = getFactory(table, true);
        Set<Table> relatedTables = getTableImmediateRelatives(table, factory, includeImplied, skippedImpliedConstraints);

        Set<DotConnector> connectors = new TreeSet<>(new SimpleConnectors(table, includeImplied).unique());
        tablesWritten.add(table);

        Map<Table, DotNode> nodes = new TreeMap<>();

        // Immediate relatives should be written first
        writeImmediateRelatives(relatedTables, tablesWritten, nodes, connectors);

        Set<Table> allCousins = new HashSet<>();
        Set<DotConnector> allCousinConnectors = new TreeSet<>();

        // next write 'cousins' (2nd degree of separation)
        if (twoDegreesOfSeparation) {
            writeCousins(relatedTables, skippedImpliedConstraints, tablesWritten, allCousinConnectors, nodes, allCousins);
        }

        // glue together any 'participants' that aren't yet connected
        // note that this is the epitome of nested loops from hell
        List<Table> participants = new ArrayList<>(nodes.keySet());
        Iterator<Table> iter = participants.iterator();
        while (iter.hasNext()) {
            Table participantA = iter.next();
            iter.remove(); // cut down the combos as quickly as possible

            for (Table participantB : participants) {
                for (DotConnector connector : new PairConnectors(participantA, participantB, false, includeImplied).unique()) {
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
        nodes.put(table, new DotNode(table, false, new DotNodeConfig(true, true), dotConfig));

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
            dot.println(node.value());
            stats.wroteTable(node.getTable());
        }

        dot.println("}");

        return skippedImpliedConstraints;
    }

    private static Factory getFactory(Table table, boolean includeExcluded) {
        Factory factory = new Default(table);
        if (includeExcluded) {
            factory = new Included(factory);
        }
        return factory;
    }

    private static Set<Table> getTableImmediateRelatives(Table table, Factory factory, boolean includeImplied, Set<ForeignKeyConstraint> skippedImpliedConstraints) {
        Columns columns = factory.columns();
        Set<TableColumn> relatedColumns = new HashSet<>();

        for (TableColumn column : columns.value()) {
            Columns children = factory.children(column);

            for (TableColumn childColumn : children.value()) {
                ForeignKeyConstraint constraint = column.getChildConstraint(childColumn);
                if (includeImplied || !constraint.isImplied())
                    relatedColumns.add(childColumn);
                else
                    skippedImpliedConstraints.add(constraint);
            }
        }

        for (TableColumn column : columns.value()) {
            Columns parents = factory.parents(column);

            for (TableColumn parentColumn : parents.value()) {
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

    private static void markExcludedColumns(Map<Table, DotNode> nodes, Set<TableColumn> excludedColumns) {
        for (TableColumn column : excludedColumns) {
            DotNode node = nodes.get(column.getTable());
            if (node != null) {
                node.excludeColumn(column);
            }
        }
    }

    private void writeImmediateRelatives(
        Set<Table> relatedTables,
        Set<Table> tablesWritten,
        Map<Table, DotNode> nodes,
        Set<DotConnector> connectors
    ) {
        for (Table relatedTable : relatedTables) {
            if (!tablesWritten.contains(relatedTable)) {
                DotNodeConfig nodeConfigurations = new DotNodeConfig(false, false);
                DotNode node = new DotNode(relatedTable, false, nodeConfigurations, dotConfig);
                nodes.put(relatedTable, node);
                connectors.addAll(new PairConnectors(relatedTable, table, true, includeImplied).unique());
            }
        }

        // connect the edges that go directly to the target table
        // so they go to the target table's type column instead
        for (DotConnector connector : connectors) {
            if (connector.pointsTo(table))
                connector.connectToParentDetails();
        }
    }

    private void writeCousins(
        Set<Table> relatedTables,
        Set<ForeignKeyConstraint> skippedImpliedConstraints,
        Set<Table> tablesWritten,
        Set<DotConnector> allCousinConnectors,
        Map<Table, DotNode> nodes,
        Set<Table> allCousins
    ) {
        for (Table relatedTable : relatedTables) {
            Set<Table> cousins = cousinsOf(relatedTable, skippedImpliedConstraints);

            for (Table cousin : cousins) {
                if (!tablesWritten.contains(cousin)) {
                    tablesWritten.add(cousin);

                    final Set<DotConnector> connectors = new PairConnectors(cousin, relatedTable, false, includeImplied).unique();
                    allCousinConnectors.addAll(connectors);

                    final DotNode node = new DotNode(cousin, false, new DotNodeConfig(), dotConfig);
                    nodes.put(cousin, node);
                }
            }

            allCousins.addAll(cousins);
        }
    }

    private Set<Table> cousinsOf(Table relatedTable, Set<ForeignKeyConstraint> skippedImpliedConstraints) {
        Factory cousinsFactory = getFactory(relatedTable, false);
        return getTableImmediateRelatives(relatedTable, cousinsFactory, includeImplied, skippedImpliedConstraints);
    }
}
