package org.schemaspy.connection;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Properties;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Tests for {@link WithPassword}.
 */
final class WithPasswordTest {

    /**
     * Given a password and an origin,
     * When the object is asked for connection properties,
     * Then its response should include the password.
     */
    @Test
    void supplyPassword() throws IOException {
        final Properties result = new WithPassword(
                "Foo",
                Properties::new
        ).properties();
        assertThat(result.containsKey("password")).isTrue();
        assertThat(result.containsValue("Foo")).isTrue();
    }

    /**
     * Given NULL password and an origin,
     * When the object is asked for connection properties,
     * Then its response should be the origin's response.
     */
    @Test
    void ignoreNull() throws IOException {
        final Properties props = new Properties();
        assertThat(
            new WithPassword(
                (String)null,
                () -> props
            ).properties()
        ).isEqualTo(props);
    }
}
