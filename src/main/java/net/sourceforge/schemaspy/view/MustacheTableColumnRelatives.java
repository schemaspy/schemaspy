package net.sourceforge.schemaspy.view;

import net.sourceforge.schemaspy.model.ForeignKeyConstraint;
import net.sourceforge.schemaspy.model.Table;
import net.sourceforge.schemaspy.model.TableColumn;

/**
 * Created by rkasa on 2016-03-24.
 */
public class MustacheTableColumnRelatives {
    private TableColumn column;
    private Table table;
    private ForeignKeyConstraint constraint;

    public MustacheTableColumnRelatives(ForeignKeyConstraint constraint) {
        this.constraint = constraint;
    }

    public MustacheTableColumnRelatives(TableColumn column, ForeignKeyConstraint constraint) {
        this(constraint);
        this.column = column;
        this.table = column.getTable();
    }

    public Table getTable() {
        return table;
    }

    public ForeignKeyConstraint getConstraint() {
        return constraint;
    }

    public TableColumn getColumn() {
        return column;
    }
}
