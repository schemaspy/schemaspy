package org.schemaspy.output.dot.schemaspy.name;

import org.junit.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Tests for {@link DefaultName}.
 */
public class DefaultNameTest {

    /**
     * When the object is asked to represent itself,
     * Then it should respond with the default name.
     */
    @Test
    public void representName() {
        final Name origin = new EmptyName();
        assertThat(
                new DefaultName(
                        origin
                ).value()
        ).isEqualTo("RelationshipsDiagram" + origin.value());
    }
}
