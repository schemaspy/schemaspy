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
}
