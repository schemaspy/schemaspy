package org.schemaspy.output.dot.schemaspy.columnsfilter;

import java.util.Collection;
import org.schemaspy.model.TableColumn;

public class Simple implements Columns {

    private final Collection<TableColumn> origin;

    public Simple(Collection<TableColumn> origin) {
        this.origin = origin;
    }

    @Override
    public Collection<TableColumn> value() {
        return this.origin;
    }
}
