package org.schemaspy.output;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.schemaspy.logging.Sanitize;
import org.schemaspy.output.diagram.Renderer;
import org.schemaspy.util.ManifestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InfoHtml {
    private final File outputDir;
    private final Renderer renderer;
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public InfoHtml(final File outputDir, final Renderer renderer) {
        this.outputDir = outputDir;
        this.renderer = renderer;
    }

    public void write() throws IOException {
        Path htmlInfoFile = outputDir.toPath().resolve("info-html.txt");
        Files.deleteIfExists(htmlInfoFile);
        writeInfo("date", ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssZ")), htmlInfoFile);
        writeInfo("os", System.getProperty("os.name") + " " + System.getProperty("os.version"), htmlInfoFile);
        writeInfo("schemaspy-version", ManifestUtils.getImplementationVersion(), htmlInfoFile);
        writeInfo("schemaspy-revision", ManifestUtils.getImplementationRevision(), htmlInfoFile);
        writeInfo("renderer", renderer.identifier(), htmlInfoFile);
    }

    private static void writeInfo(String key, String value, Path infoFile) {
        try {
            Files.write(
                infoFile,
                (key + "=" + value + "\n").getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE)
            ;
        } catch (IOException e) {
            LOGGER.error(
                "Failed to write '{}', to '{}={}'",
                new Sanitize(key),
                new Sanitize(value),
                infoFile,
                e
            );
        }
    }
}
