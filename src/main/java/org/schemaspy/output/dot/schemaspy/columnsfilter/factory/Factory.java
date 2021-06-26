package org.schemaspy.output.dot.schemaspy.columnsfilter.factory;

import org.schemaspy.model.TableColumn;
import org.schemaspy.output.dot.schemaspy.columnsfilter.Columns;

public interface Factory {
    Columns columns();
    Columns children(TableColumn column);
    Columns parents(TableColumn column);
}
