package org.schemaspy.connection;

import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Tests for {@link WithUser}.
 */
public final class WithUserTest {

    /**
     * Given a user and an origin,
     * When the object is asked for connection properties,
     * Then its response should include the user.
     */
    @Test
    public void parse() throws IOException {
        final Properties result = new WithUser(
                "Foo",
                () -> new Properties()
        ).properties();
        assertThat(result.containsKey("user")).isTrue();
        assertThat(result.containsValue("Foo")).isTrue();
    }
}
