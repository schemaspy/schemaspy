package org.schemaspy.util.naming;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Qualified}.
 */
public class QualifiedTest {

    /**
     * When the object is asked to represent itself,
     * Then it should string together parent and child.
     */
    @Test
    void representName() {
        final String parent = "foo";
        final String child = "bar";
        assertThat(
            new Qualified(() -> child, () -> parent).value()
        ).isEqualTo(parent + '.' + child);
    }

    /**
     * Given an empty parent,
     * When the object is asked to represent itself,
     * Then it should respond with the child.
     */
    @Test
    void representOrphan() {
        final String child = "apa";
        assertThat(
            new Qualified(() -> child, new EmptyName()).value()
        ).isEqualTo( child);
    }
}
