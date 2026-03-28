package org.schemaspy.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link ResourceNotFoundException}.
 */
class ResourceNotFoundExceptionTest {

    /**
     * Given a message,
     * When the object is thrown,
     * Then it provides the message and type information.
     */
    @Test
    void provideDetails() {
        var message = "foo";
        assertThatThrownBy(() -> {
            throw new ResourceNotFoundException(message);
        }).isInstanceOf(RuntimeException.class)
            .hasMessage(message);
    }
}
