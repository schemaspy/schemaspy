package org.schemaspy.connection;

import org.junit.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.io.IOException;
import java.util.Properties;

/**
 * Tests for {@link SemicolonSeparated}.
 */
public class SemicolonSeparatedTest {

    /**
     * Given a connection string that separates properties by semicolons,
     * When the object is asked for connection properties,
     * Then it should respond an object containing those properties.
     */
    @Test
    public void parse() throws IOException {
        final Properties result = new SemicolonSeparated(
                "key1\\=value1;key2\\=value2"
        ).connectionProperties();
        assertThat(result.containsKey("key1")).isTrue();
        assertThat(result.containsValue("value1")).isTrue();
        assertThat(result.containsKey("key2")).isTrue();
        assertThat(result.containsValue("value2")).isTrue();
    }
}

