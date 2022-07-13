package org.schemaspy.util.naming;

import org.junit.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Tests for {@link Concatenation}.
 */
public class ConcatenationTest {

    /**
     * When the object is asked to represent itself,
     * Then it should concatenate the second name to the first.
     */
    @Test
    public void concatenate() {
        final Name first = () -> "First";
        final Name second = () -> "Second";
        assertThat(
                new Concatenation(
                        first,
                        second
                ).value()
        ).isEqualTo(first.value() + second.value());
    }
}