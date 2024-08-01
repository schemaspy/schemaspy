package org.schemaspy.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import static org.schemaspy.input.dbms.service.ColumnLabel.TABLE_NAME;

public final class ResultSetView {

    private final Map<String, View> map;
    private final ResultSet results;

    public ResultSetView(final Map<String, View> map, final ResultSet results) {
        this.map = map;
        this.results = results;
    }

    public View view() throws SQLException {
        String view = this.results.getString("view_name");
        if (view == null) {
            view = this.results.getString(TABLE_NAME);
        }
        return this.map.get(view);
    }
}
