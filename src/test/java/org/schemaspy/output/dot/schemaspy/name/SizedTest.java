package org.schemaspy.output.dot.schemaspy.name;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Sized}.
 */
class SizedTest {

    /**
     * Given that the graph is compact,
     * When the object is asked to represent itself,
     * Then it should respond with compact.
     */
    @Test
    void interpretCompact() {
        assertThat(
                new Sized(true).value()
        ).isEqualTo("compact");
    }

    /**
     * Given that the graph is not compact,
     * When the object is asked to represent itself,
     * Then the it should respond with large.
     */
    @Test
    void interpretLarge() {
        assertThat(
                new Sized(false).value()
        ).isEqualTo("large");
    }
}
