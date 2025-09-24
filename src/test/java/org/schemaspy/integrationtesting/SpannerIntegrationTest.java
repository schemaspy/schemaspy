package org.schemaspy.integrationtesting;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.schemaspy.Main;
import org.schemaspy.cli.CommandLineArguments;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for Google Cloud Spanner support
 * Requires actual Spanner instance and credentials to run
 */
class SpannerIntegrationTest {

    @Test
    @EnabledIfEnvironmentVariable(named = "GOOGLE_CLOUD_PROJECT", matches = ".*")
    @EnabledIfEnvironmentVariable(named = "SPANNER_INSTANCE", matches = ".*")
    @EnabledIfEnvironmentVariable(named = "SPANNER_DATABASE", matches = ".*")
    void shouldGenerateSchemaForSpannerDatabase() throws Exception {
        String project = System.getenv("GOOGLE_CLOUD_PROJECT");
        String instance = System.getenv("SPANNER_INSTANCE");
        String database = System.getenv("SPANNER_DATABASE");
        
        Path outputDir = Files.createTempDirectory("spanner-test");
        
        String[] args = {
            "-t", "spanner-adc",
            "-project", project,
            "-instance", instance,
            "-db", database,
            "-o", outputDir.toString(),
            "-norows"
        };
        
        CommandLineArguments arguments = new CommandLineArguments(args);
        Main.main(arguments);
        
        // Verify output files were generated
        assertThat(new File(outputDir.toFile(), "index.html")).exists();
        assertThat(new File(outputDir.toFile(), "tables")).exists();
        
        // Cleanup
        Files.deleteIfExists(outputDir);
    }
}
