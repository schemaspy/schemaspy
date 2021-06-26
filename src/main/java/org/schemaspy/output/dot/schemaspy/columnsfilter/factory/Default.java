package org.schemaspy.output.dot.schemaspy.columnsfilter.factory;

import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.schemaspy.output.dot.schemaspy.columnsfilter.AllExcluded;
import org.schemaspy.output.dot.schemaspy.columnsfilter.Columns;
import org.schemaspy.output.dot.schemaspy.columnsfilter.Simple;

public final class Default implements Factory {
    private final Table table;
    public Default(Table table) {
        this.table = table;
    }
    @Override
    public Columns columns() {
        return new AllExcluded(new Simple(this.table.getColumns()));
    }
    @Override
    public Columns children(final TableColumn column) {
        return new AllExcluded(new Simple(column.getChildren()));
    }
    @Override
    public Columns parents(final TableColumn column) {
        return new AllExcluded(new Simple(column.getParents()));
    }
}
