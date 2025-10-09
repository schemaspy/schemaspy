package org.schemaspy.input.dbms.exceptions;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link RuntimeIOExceptionTest}.
 */
class RuntimeIOExceptionTest {

    /**
     * Given a message,
     * When the object is thrown,
     * Then it provides the message and type information.
     */
    @Test
    void provideDetails() {
        var message = "foo";
        assertThatThrownBy(() -> {
            throw new RuntimeIOException(message, new IOException());
        }).isInstanceOf(RuntimeException.class)
            .hasMessage(message);
    }
}
