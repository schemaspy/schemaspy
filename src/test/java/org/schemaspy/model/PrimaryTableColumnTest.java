package org.schemaspy.model;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link PrimaryTableColumn}.
 */
public class PrimaryTableColumnTest {

    /**
     * Given a table,
     * When the object is asked for its column,
     * Then it should ask the table for its column named ID.
     */
    @Test
    void retrievePrimaryColumn() {
        Table table = mock(Table.class);
        new PrimaryTableColumn(
            name -> table,
            () -> ""
        ).column();
        verify(table, times(1)).getColumn("ID");
    }

    /**
     * Given null,
     * When the object is asked for its column,
     * Then it should respond with null.
     */
    @Test
    void indicateAbsence() {
        assertThat(
            new PrimaryTableColumn(
                name -> null,
                () -> ""
            ).column()
        ).isNull();
    }
}
