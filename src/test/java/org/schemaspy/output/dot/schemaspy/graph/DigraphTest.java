package org.schemaspy.output.dot.schemaspy.graph;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Digraph}.
 */
class DigraphTest {

    /**
     * When the object is asked to represent itself in DOT,
     * Then it should include its name.
     */
    @Test
    void writeName() {
        final String key = "Graph";
        MatcherAssert.assertThat(
                new Digraph(
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
                new Digraph(
                        () -> "",
                        () -> key,
                        () -> ""
                ).dot(),
                CoreMatchers.containsString(key)
        );
    }

    /**
     * Given a lone node,
     * When the object is asked to represent itself in DOT,
     * Then it should include that node.
     */
    @Test
    void writeSingleElement() {
        final String key = "node";
        MatcherAssert.assertThat(
                new Digraph(
                        () -> "",
                        () -> "",
                        () -> key
                ).dot(),
                CoreMatchers.containsString(key)
        );
    }

    /**
     * Given multiple nodes,
     * When the object is asked to represent itself in DOT,
     * Then it should include both nodes.
     */
    @Test
    void writeMultipleElement() {
        final String key1 = "node1";
        final String key2 = "node2";
        final String result = new Digraph(
                () -> "",
                () -> "",
                () -> key1,
                () -> key2
        ).dot();
        MatcherAssert.assertThat(result, CoreMatchers.containsString(key1));
        MatcherAssert.assertThat(result, CoreMatchers.containsString(key2));
    }
}
