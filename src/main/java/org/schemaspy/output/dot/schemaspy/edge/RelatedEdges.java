package org.schemaspy.output.dot.schemaspy.edge;

import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.schemaspy.output.dot.schemaspy.Edge;

import java.util.HashSet;
import java.util.Set;

public class RelatedEdges implements Edges {
    private final TableColumn column;
    private final Table targetTable;
    private final boolean includeExcluded;
    private final boolean includeImplied;

    /**
     * @param column TableColumn
     * @param targetTable Table
     */
    public RelatedEdges(TableColumn column, Table targetTable, boolean includeExcluded, boolean includeImplied) {
        this.column = column;
        this.targetTable = targetTable;
        this.includeExcluded = includeExcluded;
        this.includeImplied = includeImplied;
    }

    /**
     *
     * @return Set of <code>dot</code> relationships (as {@link Edge}s)
     */
    @Override
    public Set<Edge> unique() {
        Set<Edge> relatedConnectors = new HashSet<>();
        if (!includeExcluded && column.isExcluded()) {
            return relatedConnectors;
        }

        relatedConnectors.addAll(relatedParents());

        for (TableColumn childColumn : column.getChildren()) {
            Table childTable = childColumn.getTable();
            if (isNotTarget(childTable)) {
                continue;
            }
            if (shouldExclude(childColumn)) {
                continue;
            }
            boolean implied = column.getChildConstraint(childColumn).isImplied();
            if (!implied || includeImplied) {
                relatedConnectors.add(new Edge(column, childColumn, implied));
            }
        }

        return relatedConnectors;
    }

    private Set<Edge> relatedParents() {
        final Set<Edge> relatedConnectors = new HashSet<>();
        for (TableColumn parentColumn : column.getParents()) {
            Table parentTable = parentColumn.getTable();
            if (isNotTarget(parentTable)) {
                continue;
            }
            if (shouldExclude(parentColumn)) {
                continue;
            }
            boolean implied = column.getParentConstraint(parentColumn).isImplied();
            if (!implied || includeImplied) {
                relatedConnectors.add(new Edge(parentColumn, column, implied));
            }
        }
        return relatedConnectors;
    }

    private boolean isNotTarget(final Table candidate) {
        return this.targetTable != null && candidate != this.targetTable;
    }

    private boolean shouldExclude(final TableColumn candidate) {
        return this.targetTable == null && !this.includeExcluded && candidate.isExcluded();
    }
}
