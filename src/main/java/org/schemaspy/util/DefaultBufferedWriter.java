package org.schemaspy.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

/**
 * The default {@link BufferedWriter} for SchemaSpy use.
 */
public class DefaultBufferedWriter extends BufferedWriter {
    public DefaultBufferedWriter(File file) throws IOException {
        super(
                Files.newBufferedWriter(
                        file.toPath(),
                        StandardCharsets.UTF_8,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING
                )
        );
    }
}
