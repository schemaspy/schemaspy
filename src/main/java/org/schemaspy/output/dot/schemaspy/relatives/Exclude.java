package org.schemaspy.output.dot.schemaspy.relatives;

import java.util.HashSet;
import java.util.Set;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;

public final class Exclude implements ExclusionFilter {

    private final Table table;

    public Exclude(final Table table) {
        this.table = table;
    }

    public Iterable<Verdict> children() {
        Set<Verdict> result = new HashSet<>();
        for (TableColumn column : table.getColumns()) {
            if (column.isAllExcluded()) {
                continue;
            }
            if (column.isExcluded()) {
                continue;
            }
            for (TableColumn childColumn : column.getChildren()) {
                if (childColumn.isAllExcluded()) {
                    continue;
                }
                if(childColumn.isExcluded()) {
                    continue;
                }
                result.add(new Verdict(column, childColumn));
            }
        }
        return result;
    }

    public Iterable<Verdict> parents() {
        Set<Verdict> result = new HashSet<>();
        for (TableColumn column : table.getColumns()) {
            if (column.isAllExcluded()) {
                continue;
            }
            if (column.isExcluded()) {
                continue;
            }
            for (TableColumn parentColumn : column.getParents()) {
                if (parentColumn.isAllExcluded()) {
                    continue;
                }
                if (parentColumn.isExcluded()) {
                    continue;
                }
                result.add(new Verdict(column, parentColumn));
            }
        }
        return result;
    }
}
