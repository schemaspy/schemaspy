package org.schemaspy.output.dot.schemaspy;

import org.schemaspy.model.Table;
import org.schemaspy.output.dot.RuntimeDotConfig;
import org.schemaspy.output.dot.schemaspy.graph.Digraph;
import org.schemaspy.output.dot.schemaspy.graph.Element;
import org.schemaspy.output.dot.schemaspy.graph.Graph;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class OrphanGraph implements Graph {

    private final RuntimeDotConfig runtimeDotConfig;
    private final Collection<Table> tables;

    public OrphanGraph(RuntimeDotConfig runtimeDotConfig, Collection<Table> tables) {
        this.runtimeDotConfig = runtimeDotConfig;
        this.tables = tables;
    }

    @Override
    public String dot() {
        return graph(orphans()).dot();
    }

    private List<Table> orphans() {
        return tables
                .stream()
                .filter((table -> !table.isView()))
                .filter(table -> table.isOrphan(false))
                .collect(Collectors.toList());
    }

    private Graph graph(List<Table> orphans) {
        return new Digraph(
                () -> "orphans",
                new DotConfigHeader(runtimeDotConfig, false),
                orphans.stream()
                        .sorted(Table::compareTo)
                        .map(this::asDotNode)
                        .toArray(Element[]::new)
        );
    }

    private DotNode asDotNode(Table table) {
        return new DotNode(
                table,
                true,
                new DotNodeConfig(true, true),
            runtimeDotConfig
        );
    }

}
