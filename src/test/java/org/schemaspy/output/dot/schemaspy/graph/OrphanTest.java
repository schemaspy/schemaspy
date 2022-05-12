package org.schemaspy.output.dot.schemaspy.graph;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link Orphan}.
 */
public class OrphanTest {

    /**
     * When the object is asked to represent itself in DOT,
     * Then it should delegate to its origin.
     */
    @Test
    void invokeOrigin() {
        Graph graph = mock(Graph.class);
        new Orphan(graph).dot();
        verify(graph).dot();
    }
}
