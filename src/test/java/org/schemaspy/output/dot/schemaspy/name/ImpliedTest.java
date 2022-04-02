package org.schemaspy.output.dot.schemaspy.name;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Implied}.
 */
public class ImpliedTest {

    /**
     * Given an implication,
     * When the object is asked to represent itself,
     * Then it should respond with implied.
     */
    @Test
    public void acknowledgeImplication() {
        assertThat(
            new Implied(true).value()
        ).isEqualTo("Implied");
    }

    /**
     * Given the absence of implication,
     * When the object is asked to represent itself,
     * Then it should respond with nothing.
     */
    @Test
    public void acknowledgeNonImplication() {
        assertThat(
            new Implied(false).value()
        ).isEmpty();
    }
}
