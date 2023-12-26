package org.schemaspy.output.dot.schemaspy.relationship;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link RealRelationships}.
 */
class RealRelationshipsTest {

    /**
     * When the object is asked to write itself,
     * Then it should delegate to its origin.
     */
    @Test
    void invokeOrigin() {
        final Relationships origin = mock(Relationships.class);
        new RealRelationships(origin).write();
        verify(origin).write();
    }
}
