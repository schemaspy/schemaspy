package org.schemaspy.output.dot.schemaspy.edge;

import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.schemaspy.output.dot.schemaspy.Edge;

import java.util.HashSet;
import java.util.Set;

public class SimpleEdges implements Edges {
    private final Table table;
    private final boolean includeImplied;

    /**
     *
     * @param table Table
     *
     */
    public SimpleEdges(Table table, boolean includeImplied) {
        this.table = table;
        this.includeImplied = includeImplied;
    }

    /**
     *
     * @return Set of <code>dot</code> relationships (as {@link Edge}s)
     */
    @Override
    public Set<Edge> unique() {
        Set<Edge> relationships = new HashSet<>();

        for (TableColumn column : table.getColumns()) {
            relationships.addAll(new RelatedEdges(column, null, false, includeImplied).unique());
        }

        return relationships;
    }
}
