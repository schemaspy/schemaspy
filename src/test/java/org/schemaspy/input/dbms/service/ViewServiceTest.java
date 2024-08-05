package org.schemaspy.input.dbms.service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.schemaspy.model.View;

/**
 * Tests for {@ViewService}.
 */
class ViewServiceTest {

    @Test
    void setViewComments() throws SQLException {
        final ResultSet results = mock(ResultSet.class);
        when(results.next()).thenReturn(true, false);
        final String key = "key";
        when(results.getString(anyString())).thenReturn(key);

        final PreparedStatement stmt = mock(PreparedStatement.class);
        when(stmt.executeQuery()).thenReturn(results);

        final View view = mock(View.class);
        final Map<String, View> viewsMap = mock(Map.class);
        when(viewsMap.get(key)).thenReturn(view);

        new ViewService(
            mock(SqlService.class),
            mock(Properties.class),
            mock(ColumnService.class)
        ).setViewComments(stmt, viewsMap);

        verify(view).setComments(anyString());
    }
}
