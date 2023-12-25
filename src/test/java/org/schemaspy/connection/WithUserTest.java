package org.schemaspy.connection;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Properties;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Tests for {@link WithUser}.
 */
final class WithUserTest {

    /**
     * Given a user and an origin,
     * When the object is asked for connection properties,
     * Then its response should include the user.
     */
    @Test
    void parse() throws IOException {
        final Properties result = new WithUser(
                "Foo",
                Properties::new
        ).properties();
        assertThat(result.containsKey("user")).isTrue();
        assertThat(result.containsValue("Foo")).isTrue();
    }
}
