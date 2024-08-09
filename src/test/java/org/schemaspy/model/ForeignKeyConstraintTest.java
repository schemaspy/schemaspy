package org.schemaspy.model;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ForeignKeyConstraint}.
 */
class ForeignKeyConstraintTest {

    /**
     * Given the same foreign key constraint twice,
     * When the object is asked to compare itself to the other,
     * Then it should respond that they match
     */
    @Test
    void compareSelf() {
        final Table child = mock(Table.class);
        final ForeignKeyConstraint sut = new ForeignKeyConstraint(child, "", 0, 0);
        assertThat(
            sut.compareTo(sut)
        ).isEqualTo(0);
    }

    /**
     * Given two foreign key constraints with different names,
     * When the object is asked to compare itself to the other,
     * Then it should respond that they differ.
     */
    @Test
    void compareNames() {
        final Table child = mock(Table.class);
        assertThat(
            new ForeignKeyConstraint(child, "foo", 0, 0).compareTo(
                new ForeignKeyConstraint(child, "bar", 0, 0)
            )
        ).isNotEqualTo(0);
    }

    private void mockContainer(final ForeignKeyConstraint source, final String result) {
        final Table table = source.getChildTable();
        final TableColumn childColumn = mock(TableColumn.class);
        when(childColumn.getTable()).thenReturn(table);
        source.addChildColumn(childColumn);
        when(table.getContainer()).thenReturn(result);
    }

    /**
     * Given two foreign key constraints with different containers
     * When the object is asked to compare itself to the other,
     * Then it should respond that they differ.
     */
    @Test
    void compareCrossSchemaSame() {
        final ForeignKeyConstraint sut = new ForeignKeyConstraint(
            mock(Table.class), "", 0, 0
        );
        mockContainer(sut, "foo");

        final ForeignKeyConstraint other = new ForeignKeyConstraint(
            mock(Table.class), "", 0, 0
        );
        mockContainer(other, "bar");

        assertThat(
            sut.compareTo(
            other
            )
        ).isNotEqualTo(0);
    }

    /**
     * Given two similar foreign key constraints,
     * Given that only the first one does not have a container,
     * When the object is asked to compare itself to the other,
     * Then it should respond that they differ.
     */
    @Test
    void compareCrossSchemaOursNull() {
        final ForeignKeyConstraint sut = new ForeignKeyConstraint(
            mock(Table.class), "", 0, 0
        );
        mockContainer(sut, null);

        final ForeignKeyConstraint other = new ForeignKeyConstraint(
            mock(Table.class), "", 0, 0
        );
        mockContainer(other, "bar");

        assertThat(
            sut.compareTo(
                other
            )
        ).isNotEqualTo(0);
    }

    /**
     * Given two similar foreign key constraints,
     * Given that only the first one has a container,
     * When the object is asked to compare itself to the other,
     * Then it should respond that they differ.
     */
    @Test
    void compareCrossSchemaTheirsNull() {
        final ForeignKeyConstraint sut = new ForeignKeyConstraint(
            mock(Table.class), "", 0, 0
        );
        mockContainer(sut, "foo");

        final ForeignKeyConstraint other = new ForeignKeyConstraint(
            mock(Table.class), "", 0, 0
        );
        mockContainer(other, null);

        assertThat(
            sut.compareTo(
                other
            )
        ).isNotEqualTo(0);
    }
}
