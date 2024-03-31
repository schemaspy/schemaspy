package org.schemaspy.util.naming;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link NameFromString}.
 */
public class NameFromStringTest {

    /**
     * When the object is asked for its value,
     * Then it should respond with its string.
     */
    @Test
    void representName() {
        final String value = "foo";
        assertThat(new NameFromString(value).value()).isEqualTo(value);
    }

    /**
     * Given null,
     * When the object is asked for its value,
     * Then it should respond with the null object's value.
     */
    @Test
    void representNull() {
        assertThat(
            new NameFromString(null).value()
        ).isEqualTo(new EmptyName().value());
    }
}
