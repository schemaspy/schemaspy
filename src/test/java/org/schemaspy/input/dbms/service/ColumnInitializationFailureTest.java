package org.schemaspy.input.dbms.service;

import org.junit.jupiter.api.Test;
import org.schemaspy.model.Table;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ColumnInitializationFailure}.
 */
class ColumnInitializationFailureTest {

    /**
     * Given a table and a cause,
     * When the object is thrown,
     * Then it provides a message and type information.
     */
    @Test
    void provideDetails() {
        var table = mock(Table.class);
        when(table.getType()).thenReturn("Table");
        when(table.getName()).thenReturn("Bar");
        when(table.getContainer()).thenReturn("Foo");

        assertThatThrownBy(() -> {
            throw new ColumnInitializationFailure(table, new SQLException());
        }).isInstanceOf(SQLException.class)
            .hasMessage("Failed to collect column details for Table 'Bar' in 'Foo'")
            .hasCauseInstanceOf(SQLException.class);
    }
}