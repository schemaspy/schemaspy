package org.schemaspy.output.dot.schemaspy.graph;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Orphan}.
 */
public class OrphanTest {

    /**
     * When the object is asked to represent itself in DOT,
     * Then it should include its name.
     */
    @Test
    void writeName() {
        final String key = "Graph";
        MatcherAssert.assertThat(
                new Orphan(
                        () -> key,
                        () -> "",
                        () -> ""
                ).dot(),
                CoreMatchers.containsString("digraph \"" + key + "\" {")
        );
    }

    /**
     * When the object is asked to represent itself in DOT,
     * Then it should include its header.
     */
    @Test
    void writeHeader() {
        final String key = "header";
        MatcherAssert.assertThat(
                new Orphan(
                        () -> "",
                        () -> key,
                        () -> ""
                ).dot(),
                CoreMatchers.containsString(key)
        );
    }

    /**
     * When the object is asked to represent itself in DOT,
     * Then it should include its node.
     */
    @Test
    void writeNode() {
        final String key = "node";
        MatcherAssert.assertThat(
                new Orphan(
                        () -> "",
                        () -> "",
                        () -> key
                ).dot(),
                CoreMatchers.containsString(key)
        );
    }
}
