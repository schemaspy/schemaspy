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
import org.schemaspy.output.dot.RuntimeDotConfig;
import org.schemaspy.output.dot.schemaspy.columnsfilter.Columns;
import org.schemaspy.output.dot.schemaspy.columnsfilter.factory.Default;
import org.schemaspy.output.dot.schemaspy.columnsfilter.factory.Factory;
import org.schemaspy.output.dot.schemaspy.columnsfilter.factory.Included;
import org.schemaspy.output.dot.schemaspy.edge.PairEdges;
import org.schemaspy.output.dot.schemaspy.edge.SimpleEdges;
import org.schemaspy.output.dot.schemaspy.graph.Digraph;
import org.schemaspy.output.dot.schemaspy.graph.Element;
import org.schemaspy.output.dot.schemaspy.name.DefaultName;
import org.schemaspy.output.dot.schemaspy.name.Degree;
import org.schemaspy.output.dot.schemaspy.name.Implied;
import org.schemaspy.output.dot.schemaspy.relationship.Relationships;
import org.schemaspy.util.naming.Concatenation;
import org.schemaspy.util.naming.Name;

import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

/**
 * Format table data into .dot format to feed to Graphvis' dot program.
 *
 * @author John Currier
 * @author Rafal Kasa
 * @author Nils Petzaell
 */
public class DotTableFormatter implements Relationships {

    private final RuntimeDotConfig runtimeDotConfig;
    private final Table table;
    private final boolean twoDegreesOfSeparation;
    private final LongAdder stats;
    private final boolean includeImplied;
    private final PrintWriter dot;
    private final Header header;
    private final Name graph;

    public DotTableFormatter(
            final RuntimeDotConfig runtimeDotConfig,
            final Table table,
            final boolean twoDegreesOfSeparation,
            final LongAdder stats,
            final boolean includeImplied,
            final PrintWriter dot
    ) {
        this(
            runtimeDotConfig,
                table,
                twoDegreesOfSeparation,
                stats,
                includeImplied,
                dot,
                new DotConfigHeader(runtimeDotConfig),
                new Concatenation(
                        new Degree(twoDegreesOfSeparation),
                        new Concatenation(
                                new DefaultName(),
                                new Implied(includeImplied)
                        )
                )
        );
    }

    public DotTableFormatter(
        final RuntimeDotConfig runtimeDotConfig,
        final Table table,
        final boolean twoDegreesOfSeparation,
        final LongAdder stats,
        final boolean includeImplied,
        final PrintWriter dot,
        final Header header,
        final Name graph
    ) {
        this.runtimeDotConfig = runtimeDotConfig;
        this.table = table;
        this.twoDegreesOfSeparation = twoDegreesOfSeparation;
        this.stats = stats;
        this.includeImplied = includeImplied;
        this.dot = dot;
        this.header = header;
        this.graph = graph;
    }

    @Override
    public void write() {
        writeTableRelationships();
    }

    /**
     * Write relationships associated with the given table.<p>
     * Returns a set of the implied constraints that could have been included but weren't.
     */
    private void writeTableRelationships() {
        Set<Table> tablesWritten = new HashSet<>();

        Factory factory = getFactory(table, true);
        Set<Table> relatedTables = immediateRelatives(table, factory, includeImplied);

        Set<Edge> edges = new TreeSet<>(new SimpleEdges(table, includeImplied).unique());
        tablesWritten.add(table);

        Map<Table, DotNode> nodes = new TreeMap<>();

        // First, write immediate relatives
        final List<Table> unwritten = relatedTables.stream()
                .filter(relative -> !tablesWritten.contains(relative))
                .collect(Collectors.toList());
        nodes.putAll(immediateRelativesNodes(unwritten));
        edges.addAll(immediateRelativesEdges(unwritten));

        connectEdges(edges);

        Set<Table> allCousins = new HashSet<>();
        Set<Edge> allCousinEdges = new TreeSet<>();

        // next write 'cousins' (2nd degree of separation)
        if (twoDegreesOfSeparation) {
            writeCousins(relatedTables, tablesWritten, allCousinEdges, nodes, allCousins);
        }

        // glue together any 'participants' that aren't yet connected
        // note that this is the epitome of nested loops from hell
        List<Table> participants = new ArrayList<>(nodes.keySet());
        Iterator<Table> iter = participants.iterator();
        while (iter.hasNext()) {
            Table participantA = iter.next();
            iter.remove(); // cut down the combos as quickly as possible

            for (Table participantB : participants) {
                for (Edge edge : new PairEdges(participantA, participantB, false, includeImplied).unique()) {
                    if (twoDegreesOfSeparation && (allCousins.contains(participantA) || allCousins.contains(participantB))) {
                        allCousinEdges.add(edge);
                    } else {
                        edges.add(edge);
                    }
                }
            }
        }

        // now directly connect the loose ends to the title of the
        // 2nd degree of separation tables
        for (Edge edge : allCousinEdges) {
            if (allCousins.contains(edge.getParentTable()) && !relatedTables.contains(edge.getParentTable()))
                edge.connectToParentTitle();
            if (allCousins.contains(edge.getChildTable()) && !relatedTables.contains(edge.getChildTable()))
                edge.connectToChildTitle();
        }

        // include the table itself
        nodes.put(table, new DotNode(table, false, new DotNodeConfig(true, true), runtimeDotConfig));

        edges.addAll(allCousinEdges);
        
        for (Edge edge : edges) {
            if (edge.isImplied()) {
                DotNode node = nodes.get(edge.getParentTable());
                if (node != null) {
                    node.setShowImplied(true);
                }
            }
        }
        for (Edge edge : edges) {
            if (edge.isImplied()) {
                DotNode node = nodes.get(edge.getChildTable());
                if (node != null) {
                    node.setShowImplied(true);
                }
            }
        }

        List<Element> elements = new LinkedList<>();
        elements.addAll(edges);
        elements.addAll(nodes.values());
        stats.add(nodes.size());

        dot.println(
            new Digraph(
                graph,
                header,
                elements.stream().toArray(Element[]::new)
            ).dot()
        );
    }

    private static Factory getFactory(Table table, boolean includeExcluded) {
        Factory factory = new Default(table);
        if (includeExcluded) {
            factory = new Included(factory);
        }
        return factory;
    }

    private static Set<Table> immediateRelatives(Table table, Factory factory, boolean includeImplied) {
        Set<Table> relatedTables = new HashSet<>();
        relatedTables.addAll(childrenTables(factory, includeImplied));
        relatedTables.addAll(parentsTables(factory, includeImplied));
        relatedTables.remove(table);
        return relatedTables;
    }

    private static Set<Table> childrenTables(Factory factory, boolean includeImplied) {
        Columns columns = factory.columns();
        Set<Table> result = new HashSet<>();
        for (TableColumn column : columns.value()) {
            Columns children = factory.children(column);
            for (TableColumn childColumn : children.value()) {
                ForeignKeyConstraint constraint = column.getChildConstraint(childColumn);
                if (includeImplied || !constraint.isImplied()) {
                    result.add(childColumn.getTable());
                }
            }
        }
        return result;
    }

    private static Set<Table> parentsTables(Factory factory, boolean includeImplied) {
        Columns columns = factory.columns();
        Set<Table> result = new HashSet<>();
        for (TableColumn column : columns.value()) {
            Columns parents = factory.parents(column);

            for (TableColumn parentColumn : parents.value()) {
                ForeignKeyConstraint constraint = column.getParentConstraint(parentColumn);
                if (includeImplied || !constraint.isImplied()) {
                    result.add(parentColumn.getTable());
                }
            }
        }
        return result;
    }

    private Map<Table, DotNode> immediateRelativesNodes(final List<Table> tables) {
        final Map<Table, DotNode> result = new HashMap<>();
        for (Table relatedTable : tables) {
            DotNodeConfig nodeConfigurations = new DotNodeConfig(false, false);
            DotNode node = new DotNode(relatedTable, false, nodeConfigurations, runtimeDotConfig);
            result.put(relatedTable, node);
        }
        return result;
    }

    private Set<Edge> immediateRelativesEdges(final List<Table> tables) {
        final Set<Edge> result = new HashSet<>();
        for (Table relatedTable : tables) {
            result.addAll(new PairEdges(relatedTable, table, true, includeImplied).unique());
        }
        return result;
    }

    private void connectEdges(final Set<Edge> edges) {
        for (Edge edge : edges) {
            edge.connectToDetailsLogically(table);
        }
    }

    private void writeCousins(
        Set<Table> relatedTables,
        Set<Table> tablesWritten,
        Set<Edge> allCousinEdges,
        Map<Table, DotNode> nodes,
        Set<Table> allCousins
    ) {
        for (Table relatedTable : relatedTables) {
            Set<Table> cousins = cousinsOf(relatedTable);

            final List<Table> missing = cousins.stream()
                    .filter(cousin -> !tablesWritten.contains(cousin))
                    .collect(Collectors.toList());

            for (Table cousin : missing) {
                final Set<Edge> edges = new PairEdges(cousin, relatedTable, false, includeImplied).unique();
                allCousinEdges.addAll(edges);
            }

            for (Table cousin : missing) {
                final DotNode node = new DotNode(cousin, false, new DotNodeConfig(), runtimeDotConfig);
                nodes.put(cousin, node);
            }

            tablesWritten.addAll(missing);
            allCousins.addAll(cousins);
        }
    }

    private Set<Table> cousinsOf(Table relatedTable) {
        Factory cousinsFactory = getFactory(relatedTable, false);
        return immediateRelatives(relatedTable, cousinsFactory, includeImplied);
    }
}
