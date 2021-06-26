package org.schemaspy.output.dot.schemaspy.columnsfilter.factory;

import org.schemaspy.model.TableColumn;
import org.schemaspy.output.dot.schemaspy.columnsfilter.Columns;
import org.schemaspy.output.dot.schemaspy.columnsfilter.Excluded;

public final class Included implements Factory {
    private final Factory origin;
    public Included(final Factory origin) {
        this.origin = origin;
    }
    @Override
    public Columns columns() {
        return new Excluded(this.origin.columns());
    }
    @Override
    public Columns children(final TableColumn column) {
        return new Excluded(this.origin.children(column));
    }
    @Override
    public Columns parents(final TableColumn column) {
        return new Excluded(this.origin.parents(column));
    }
}
