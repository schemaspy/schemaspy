package org.schemaspy.util.naming;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Tests for {@link EmptyName}.
 */
class EmptyNameTest {

    /**
     * When the object is asked to represent itself,
     * Then it should respond with the empty string.
     */
    @Test
    void representName() {
        assertThat(new EmptyName().value()).isEmpty();
    }

    /**
     * When the object is asked if it equals another,
     * Then it should discriminate based on value.
     */
    @Test
    void equalName() {
        assertThat(
            new EmptyName().equals((Name) () -> "")
        ).isTrue();
    }

    /**
     * When the object is asked for its hash code,
     * Then it should not change its response.
     */
    @Test
    void consistentHashCode() {
        final Name sut = new EmptyName();
        assertThat(
            sut.hashCode()
        ).isEqualTo(sut.hashCode());
    }
}
