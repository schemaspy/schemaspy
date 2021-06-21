package org.schemaspy.output.dot.schemaspy.relatives;

import org.schemaspy.model.TableColumn;

public final class Verdict {

    /*
     * This class will be removed in the future.
     *
     * It's little more than a stepping stone for making a refactoring safer.
     */

    private final TableColumn column;
    private final TableColumn relative;

    public Verdict(final TableColumn column, final TableColumn relative) {
        this.column = column;
        this.relative = relative;
    }

    public TableColumn column() {
        return column;
    }

    public TableColumn relative() {
        return relative;
    }
}