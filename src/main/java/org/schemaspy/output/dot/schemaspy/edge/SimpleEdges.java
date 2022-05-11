package org.schemaspy.output.dot.schemaspy.edge;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.schemaspy.output.dot.schemaspy.Edge;

public class SimpleEdges implements Edges {
    private final Table table;
    private final boolean includeImplied;

    /**
     *
     * @param table Table
     * @throws IOException
     * @return Set of <code>dot</code> relationships (as {@link Edge}s)
     */
    public SimpleEdges(Table table, boolean includeImplied) {
        this.table = table;
        this.includeImplied = includeImplied;
    }

        @Override
    public Set<Edge> unique() {
        Set<Edge> relationships = new HashSet<>();

        for (TableColumn column : table.getColumns()) {
            relationships.addAll(new RelatedEdges(column, null, false, includeImplied).unique());
        }

        return relationships;
    }
}
