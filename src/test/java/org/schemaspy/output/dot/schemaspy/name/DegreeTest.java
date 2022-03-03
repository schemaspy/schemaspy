package org.schemaspy.output.dot.schemaspy.name;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Degree}.
 */
public class DegreeTest {

    /**
     * Given two degrees of separation,
     * When the object is asked to represent itself,
     * Then the origin's representation should be prepended with appropriate indication.
     */
    @Test
    public void acknowledgeImplication() {
        final Name origin = new EmptyName();
        assertThat(
            new Degree(true, origin).value()
        ).isEqualTo("twoDegrees" + origin.value());
    }

    /**
     * Given one degree of separation,
     * When the object is asked to represent itself,
     * Then the origin's representation should be provided.
     */
    @Test
    public void acknowledgeNonImplication() {
        final Name origin = new EmptyName();
        assertThat(
            new Degree(false, origin).value()
        ).isEqualTo("oneDegree" + origin.value());
    }
}
