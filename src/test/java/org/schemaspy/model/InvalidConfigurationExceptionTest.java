package org.schemaspy.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * Tests for {@link InvalidConfigurationException}.
 */
class InvalidConfigurationExceptionTest {

    /**
     * Given a message and a cause,
     * When the object is thrown,
     * Then it provides the message and the cause.
     */
    @Test
    void provideDetails() {
        var message = "foo";
        var cause = new Throwable();
        assertThatThrownBy(() -> {
            throw new InvalidConfigurationException(message, cause);
        }).hasMessage(message).hasCause(cause);
    }
}
