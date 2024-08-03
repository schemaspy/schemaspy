package org.schemaspy.model;

import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;
import static org.schemaspy.input.dbms.service.ColumnLabel.COLUMN_NAME;

/**
 * Tests for {@link ResultSetTableColumn}.
 */
public class ResultSetTableColumnTest {

    @Test
    void retrieveColumn() throws SQLException {
        String name = "name";

        ResultSet results = mock(ResultSet.class);
        when(results.getString(COLUMN_NAME)).thenReturn(name);
        Table table = mock(Table.class);

        new ResultSetTableColumn(table, results).column();
        verify(table, times(1)).getColumn(name);
    }

    @Test
    void nullCheck() throws SQLException {
        String name = "name";

        ResultSet results = mock(ResultSet.class);
        when(results.getString(COLUMN_NAME)).thenReturn(name);

        assertThat(
                new ResultSetTableColumn(null, results).column()
        ).isNull();
    }
}
