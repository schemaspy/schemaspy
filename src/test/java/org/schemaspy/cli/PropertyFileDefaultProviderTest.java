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
package org.schemaspy.cli;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Nils Petzaell
 */
class PropertyFileDefaultProviderTest {

    @TempDir
    private static Path temporaryFolder;

    private static PropertyFileDefaultProvider propertyFileDefaultProvider;

    @BeforeAll
    static void createPropertiesFile() throws IOException {
        File propertiesFile = Files.createFile(temporaryFolder.resolve("schemaspy.properties")).toFile();
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(propertiesFile.toPath(), StandardCharsets.UTF_8)) {
            bufferedWriter.write("schemaspy.user=humbug");
        }
        propertyFileDefaultProvider = new PropertyFileDefaultProvider(propertiesFile.getAbsolutePath());
    }

    @Test
    void getStringValue() {
        assertThat(propertyFileDefaultProvider.getDefaultValueFor("schemaspy.user")).isEqualTo("humbug");
    }

}