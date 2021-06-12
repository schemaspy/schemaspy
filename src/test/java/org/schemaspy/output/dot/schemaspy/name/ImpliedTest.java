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
     * Then the origin's representation should be prepended with appropriate indication.
     */
    @Test
    public void acknowledgeImplication() {
        final Name origin = new EmptyName();
        assertThat(
            new Implied(true, origin).value()
        ).isEqualTo("Implied" + origin.value());
    }

    /**
     * Given the absence of implication,
     * When the object is asked to represent itself,
     * Then the origin's representation should be provided.
     */
    @Test
    public void acknowledgeNonImplication() {
        final Name origin = new EmptyName();
        assertThat(
            new Implied(false, origin).value()
        ).isEqualTo(origin.value());
    }
}
