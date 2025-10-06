package org.schemaspy.input.dbms.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link InconsistencyException}.
 */
class InconsistencyExceptionTest {

    /**
     * Given a message,
     * When the object is thrown,
     * Then it provides the message and type information.
     */
    @Test
    void provideDetails() {
        var message = "foo";
        assertThatThrownBy(() -> {
            throw new InconsistencyException(message);
        }).isInstanceOf(RuntimeException.class)
            .hasMessage(message);
    }
}
