package org.schemaspy.view;

import org.schemaspy.model.ForeignKeyConstraint;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;

/**
 * Created by rkasa on 2016-03-24.
 */
public class MustacheTableColumnRelatives {
    private TableColumn column;
    private Table table;
    private ForeignKeyConstraint constraint;
    private String path;

    public MustacheTableColumnRelatives(ForeignKeyConstraint constraint) {
        this.constraint = constraint;
    }

    public MustacheTableColumnRelatives(TableColumn column, ForeignKeyConstraint constraint) {
        this(constraint);
        this.column = column;
        this.table = column.getTable();
        this.path = table.isRemote() ? ("../../" + table.getContainer() + "/tables/") : "";
    }

    public Table getTable() {
        return table;
    }

    public String getPath() {
        return path;
    }

    public ForeignKeyConstraint getConstraint() {
        return constraint;
    }

    public TableColumn getColumn() {
        return column;
    }
}
