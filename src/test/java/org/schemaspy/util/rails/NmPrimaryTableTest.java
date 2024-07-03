package org.schemaspy.util.rails;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import org.junit.jupiter.api.Test;
import org.schemaspy.util.naming.NameFromStringTest;

/**
 * Tests for {@link NameFromStringTest}.
 */
public class NmPrimaryTableTest {

    /**
     * Given a column name adhering to the rails naming convention,
     * When the object is asked for its value,
     * Then it should respond with the name of the primary table.
     */
    @Test
    void inferTable() {
        assertThat(
            new NmPrimaryTable("example_id").value()
        ).isEqualTo("examples");
    }

    /**
     * Given a column name that does not adhere to the rails naming convention,
     * When the object is asked for its value,
     * Then it should respond with the empty string.
     */
    @Test
    void inferNothing() {
        assertThat(
            new NmPrimaryTable("example").value()
        ).isEqualTo("");
    }
}
