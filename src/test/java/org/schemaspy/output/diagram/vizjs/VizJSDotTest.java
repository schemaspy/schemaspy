/*
 * Copyright (C) 2018 Nils Petzaell
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
package org.schemaspy.output.diagram.vizjs;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.linesOf;

public class VizJSDotTest {

    private static Path input = Paths.get("src", "test", "resources", "vizjs");

    private static VizJSDot javaDotViz = new VizJSDot();

    @TempDir
    private static Path tempDir;

    @ParameterizedTest
    @ValueSource(strings = {
        "orphans.group.1degree",
        "orphans.user.1degree",
        "relationships.implied.compact",
        "relationships.implied.large",
        "tables.group.1degree",
        "tables.group.implied1degrees",
        "tables.user.1degree",
        "tables.user.implied1degrees",
        "tables.userAndGroup.1degree"
    })
    void generateSVG(String name) throws IOException {
        File dotFile = input.resolve(name + ".dot").toFile();
        File expect = input.resolve(name + ".svg").toFile();
        File actual = Files.createFile(tempDir.resolve(name + ".svg")).toFile();
        javaDotViz.generateDiagram(dotFile, actual);
        assertThat(linesOf(actual, StandardCharsets.UTF_8))
            .isEqualTo(linesOf(expect, StandardCharsets.UTF_8));
    }
}