package org.schemaspy.model;

import java.util.Collections;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link TablesMap}.
 */
public class TablesMapTest {

    /**
     * When the object is asked for a table that is in the collection,
     * Then it should respond with the table.
     */
    @Test
    void provideTable() {
        final Table table = mock(Table.class);
        final String name = "foo";

        assertThat(
            new TablesMap(
                Collections.singletonMap(name, table)
            ).table(() -> name)
        ).isEqualTo(table);
    }

    /**
     * When the object is asked for a table that is not in the collection,
     * Then it should respond with nothing.
     */
    @Test
    void indicateAbsence() {
        assertThat(
            new TablesMap(
                Collections.singletonMap("foo", mock(Table.class))
            ).table(() -> "bar")
        ).isEqualTo(null);
    }
}
