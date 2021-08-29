package org.schemaspy.output.dot.schemaspy.connectors;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.schemaspy.output.dot.schemaspy.DotConnector;

public class RelatedConnectors implements DotConnectors {
    private final TableColumn column;
    private final Table targetTable;
    private final boolean includeExcluded;
    private final boolean includeImplied;

    /**
     * @param column TableColumn
     * @param targetTable Table
     * @throws IOException
     * @return Set of <code>dot</code> relationships (as {@link DotConnector}s)
     */
    public RelatedConnectors(TableColumn column, Table targetTable, boolean includeExcluded, boolean includeImplied) {
        this.column = column;
        this.targetTable = targetTable;
        this.includeExcluded = includeExcluded;
        this.includeImplied = includeImplied;
    }

    @Override
    public Set<DotConnector> unique() {
        Set<DotConnector> relatedConnectors = new HashSet<>();
        if (!includeExcluded && column.isExcluded())
            return relatedConnectors;

        for (TableColumn parentColumn : column.getParents()) {
            Table parentTable = parentColumn.getTable();
            if (targetTable != null && parentTable != targetTable)
                continue;
            if (targetTable == null && !includeExcluded && parentColumn.isExcluded())
                continue;
            boolean implied = column.getParentConstraint(parentColumn).isImplied();
            if (!implied || includeImplied) {
                relatedConnectors.add(new DotConnector(parentColumn, column, implied));
            }
        }

        for (TableColumn childColumn : column.getChildren()) {
            Table childTable = childColumn.getTable();
            if (targetTable != null && childTable != targetTable)
                continue;
            if (targetTable == null && !includeExcluded && childColumn.isExcluded())
                continue;
            boolean implied = column.getChildConstraint(childColumn).isImplied();
            if (!implied || includeImplied) {
                relatedConnectors.add(new DotConnector(column, childColumn, implied));
            }
        }

        return relatedConnectors;
    }
}
