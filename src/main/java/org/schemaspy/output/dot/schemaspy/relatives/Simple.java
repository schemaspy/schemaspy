package org.schemaspy.output.dot.schemaspy.relatives;

import java.util.Collection;
import java.util.List;
import org.schemaspy.model.TableColumn;

public class Simple implements Columns {

    private final List<TableColumn> origin;

    public Simple(List<TableColumn> origin) {
        this.origin = origin;
    }

    @Override
    public Collection<TableColumn> value() {
        return this.origin;
    }
}
