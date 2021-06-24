package org.schemaspy.output.dot.schemaspy.relatives;

import java.util.Collection;
import java.util.stream.Collectors;
import org.schemaspy.model.TableColumn;

public final class Excluded implements Columns {

    private final Columns origin;

    public Excluded(final Columns origin) {
        this.origin = origin;
    }

    @Override
    public Collection<TableColumn> value() {
        return this.origin.value().stream()
            .filter(column -> !column.isExcluded())
            .collect(Collectors.toList());
    }
}
