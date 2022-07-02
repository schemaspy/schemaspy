package org.schemaspy.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

/**
 * The default {@link PrintWriter} for SchemaSpy use.
 */
public class DefaultPrintWriter extends PrintWriter {
    public DefaultPrintWriter(File file) throws IOException {
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
