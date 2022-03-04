package org.schemaspy.output.dot.schemaspy.name;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Degree}.
 */
public class SizedTest {

    /**
     * Given that the graph is compact,
     * When the object is asked to represent itself,
     * Then the response should reflect this.
     */
    @Test
    public void interpretCompact() {
        final Name origin = new EmptyName();
        assertThat(
                new Sized(true, origin).value()
        ).isEqualTo("compact" + origin.value());
    }

    /**
     * Given that the graph is not compact,
     * When the object is asked to represent itself,
     * Then the response should reflect that the graph is large.
     */
    @Test
    public void interpretLarge() {
        final Name origin = new EmptyName();
        assertThat(
                new Sized(false, origin).value()
        ).isEqualTo("large" + origin.value());
    }
}
