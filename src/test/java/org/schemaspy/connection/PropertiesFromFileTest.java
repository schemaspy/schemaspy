package org.schemaspy.connection;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PropertiesFromFile}.
 */
class PropertiesFromFileTest {

    /**
     * Given a file with correctly formatted contents,
     * When the object asked to produce a properties object,
     * Then its response reflects the file contents.
     */
    @Test
    void readFile(@TempDir Path tempDir) throws IOException {
        var key = "foo";
        var value = "bar";
        var tempFile = tempDir.resolve("foobar.txt");
        Files.write(
            tempFile,
            String.format("%s=%s", key, value).getBytes()
        );

        var result = new PropertiesFromFile(tempFile.toString()).properties();
        assertThat(result.containsKey(key)).isTrue();
        assertThat(result.containsValue(value)).isTrue();
    }
}
