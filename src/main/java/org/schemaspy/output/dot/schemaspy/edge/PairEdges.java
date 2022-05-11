package org.schemaspy.output.dot.schemaspy.edge;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.schemaspy.output.dot.schemaspy.Edge;

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
     * @throws IOException
     * @return Set of <code>dot</code> relationships (as {@link Edge}s)
     */
    public PairEdges(Table table1, Table table2, boolean includeExcluded, boolean includeImplied) {
        this.table1 = table1;
        this.table2 = table2;
        this.includeExcluded = includeExcluded;
        this.includeImplied = includeImplied;
    }

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
