package org.schemaspy.output.dot.schemaspy.edge;

import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.schemaspy.output.dot.schemaspy.Edge;

import java.util.HashSet;
import java.util.Set;

public class PairEdges implements Edges {

    private final Table table1;
    private final Table table2;
    private final boolean includeExcluded;
    private final boolean includeImplied;

    /**
     * Get all the relationships that exist between these two tables.
     *
     * @param table1 Table
     * @param table2 Table
     */
    public PairEdges(Table table1, Table table2, boolean includeExcluded, boolean includeImplied) {
        this.table1 = table1;
        this.table2 = table2;
        this.includeExcluded = includeExcluded;
        this.includeImplied = includeImplied;
    }

    /**
     *
     * @return Set of <code>dot</code> relationships (as {@link Edge}s)
     */
    @Override
    public Set<Edge> unique() {
        Set<Edge> relationships = new HashSet<>();

        for (TableColumn column : table1.getColumns()) {
            relationships.addAll(new RelatedEdges(column, table2, includeExcluded, includeImplied).unique());
        }

        for (TableColumn column : table2.getColumns()) {
            relationships.addAll(new RelatedEdges(column, table1, includeExcluded, includeImplied).unique());
        }

        return relationships;
    }
}
