package org.schemaspy.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.schemaspy.input.dbms.service.ColumnLabel.COLUMN_NAME;
import static org.schemaspy.input.dbms.service.ColumnLabel.COMMENTS;

public final class ResultSetTableColumn {

    private final Table table;
    private final ResultSet results;

    public ResultSetTableColumn(final Table table, final ResultSet results) {
        this.table = table;
        this.results = results;
    }

    public TableColumn column() throws SQLException {
        if (this.table != null) {
            return this.table.getColumn(this.results.getString(COLUMN_NAME));
        }
        return null;
    }
}
