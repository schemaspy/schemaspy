package org.schemaspy.util.copy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Tests for {@link NullCopy}.
 */
class NullCopyTest {

    /**
     * When the object is asked to copy,
     * Then it does nothing.
     */
    @Test
    void doNothing() {
        assertDoesNotThrow(
            () -> new NullCopy().copy()
        );
    }
}
