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
     * Then it should respond with two degrees.
     */
    @Test
    public void acknowledgeImplication() {
        assertThat(
            new Degree(true).value()
        ).isEqualTo("twoDegrees");
    }

    /**
     * Given one degree of separation,
     * When the object is asked to represent itself,
     * Then it should respond with one degree.
     */
    @Test
    public void acknowledgeNonImplication() {
        assertThat(
            new Degree(false).value()
        ).isEqualTo("oneDegree");
    }
}
