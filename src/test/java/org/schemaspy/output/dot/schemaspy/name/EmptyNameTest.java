package org.schemaspy.output.dot.schemaspy.name;

import org.junit.Test;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Tests for {@link EmptyName}.
 */
public class EmptyNameTest {

    /**
     * When the object is asked to represent itself,
     * Then it should respond with the empty string.
     */
    @Test
    public void representName() {
        assertThat(new EmptyName().value()).isEmpty();
    }
}
