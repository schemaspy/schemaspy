package org.schemaspy.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link RailsForeignKeyConstraint}.
 */
class RailsForeignKeyConstraintTest {

    /**
     * When the object is asked for its name,
     * Then it responds that it was inferred by the rails naming convention.
     */
    @Test
    void provideName() {
        var table = mock(Table.class);
        when(table.getFullName()).thenReturn("");
        var parent = mock(TableColumn.class);
        when(parent.getTable()).thenReturn(table);
        var child = mock(TableColumn.class);
        when(child.getTable()).thenReturn(table);
        assertThat(
            new RailsForeignKeyConstraint(
                parent,
                child
            ).getName()
        ).isEqualTo("ByRailsConventionConstraint");
    }
}
