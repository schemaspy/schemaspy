/*
 * Copyright (C) 2019 Nils Petzaell
 * Copyright (C) 2023 Samuel Dussault
 *
 * This file is part of SchemaSpy.
 *
 * SchemaSpy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SchemaSpy. If not, see <http://www.gnu.org/licenses/>.
 */
package org.schemaspy.integrationtesting;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.schemaspy.testing.SchemaSpyRunnerFixture.schemaSpyRunner;

/**
 * @author Nils Petzaell
 * @author Samuel Dussault
 */
class MarkupIT {

    @Test
    void defaultMarkupIsMarkdown() throws IOException {
        Path outDir = Paths.get("target","testout", "integrationtesting","sqlite-markdown");
        String description = "## Title in Markdown";
        String expectedHtmlDescription = "<h2><a href=\"#title-in-markdown\" id=\"title-in-markdown\">Title in Markdown</a></h2>";

        String[] args = {
                "-t", "sqlite-xerial",
                "-db", "src/test/resources/integrationTesting/sqlite/database/chinook.db",
                "-s", "chinook",
                "-cat", "chinook",
                "-o", outDir.toString(),
                "-sso",
                "-vizjs",
                "-desc", description
        };
        schemaSpyRunner(args).run();

        String actualHtml = read(outDir.resolve("index.html"));

        assertThat(actualHtml).contains(expectedHtmlDescription);
    }

    @Test
    void asciiDocParameterGeneratesHtmlFromAsciidocDescription() throws IOException {
        Path outDir = Paths.get("target","testout", "integrationtesting","sqlite-asciidoc");
        String description = "== Title in Asciidoc";
        String expectedHtmlDescription = "<h2 id=\"_title_in_asciidoc\">Title in Asciidoc</h2>";

        String[] args = {
                "-t", "sqlite-xerial",
                "-db", "src/test/resources/integrationTesting/sqlite/database/chinook.db",
                "-s", "chinook",
                "-cat", "chinook",
                "-o", outDir.toString(),
                "-sso",
                "-vizjs",
                "-asciidoc",
                "-desc", description
        };
        schemaSpyRunner(args).run();

        String actualHtml = read(outDir.resolve("index.html"));

        assertThat(actualHtml).contains(expectedHtmlDescription);
    }

    private String read(Path filePath) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (Stream<String> stream = Files.lines(filePath, StandardCharsets.UTF_8)) {
            stream.forEach(s -> builder.append(s).append("\n"));
        }
        return builder.toString();
    }
}
