package org.schemaspy.output.dot.schemaspy.name;

import org.junit.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Tests for {@link DefaultName}.
 */
public class DefaultNameTest {

    /**
     * When the object is asked to represent itself,
     * Then it should respond with relationships diagram.
     */
    @Test
    public void representName() {
        assertThat(
                new DefaultName().value()
        ).isEqualTo("RelationshipsDiagram");
    }
}
