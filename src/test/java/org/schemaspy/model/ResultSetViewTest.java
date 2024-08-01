package org.schemaspy.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ResultSetView}.
 */
class ResultSetViewTest {

    @Test
    void retrieveView() throws SQLException {
        String name = "name";

        ResultSet results = mock(ResultSet.class);
        when(results.getString("view_name")).thenReturn(name);

        View view = mock(View.class);
        Map<String, View> map = mock(Map.class);
        when(map.get(name)).thenReturn(view);

        assertThat(
            new ResultSetView(map, results).view()
        ).isEqualTo(view);
    }

    @Test
    void fallbackOnTable() throws SQLException {
        String name = "name";

        ResultSet results = mock(ResultSet.class);
        when(results.getString("view_name")).thenReturn(null);
        when(results.getString("table_name")).thenReturn(name);

        View view = mock(View.class);
        Map<String, View> map = mock(Map.class);
        when(map.get(name)).thenReturn(view);

        assertThat(
            new ResultSetView(map, results).view()
        ).isEqualTo(view);
    }
}
