/*
 * Copyright (C) 2017 Daniel Watt
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
package org.schemaspy.view;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Daniel Watt
 */
public class MustacheWriterTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testReader() throws IOException {
        File file = tempFolder.newFile("test.txt");
        FileUtils.write(file, "test", "UTF-8");

        String data;
        try (Reader reader = MustacheWriter.getReader(tempFolder.getRoot().getAbsolutePath(), file.getName())) {
            data = IOUtils.toString(reader);
        }
        assertThat(data).isEqualTo("test");
    }

    @Test
    public void classpathResource() throws IOException {
        String data;
        try (Reader reader = MustacheWriter.getReader("", "banner.txt")) {
            data = IOUtils.toString(reader);
        }
        assertThat(data).contains("SchemaSpy");
    }

    @Test
    public void write() throws IOException {
        File outputFolder = tempFolder.newFolder("output");
        Map<String, Object> scopes = Collections.singletonMap("scopeVariable","test");

        MustacheWriter writer = new MustacheWriter(outputFolder, scopes, tempFolder.getRoot().getAbsolutePath(), "database", false);

        writer.write("test.html", "out.html", "test.js");

        String data = FileUtils.readFileToString(new File(outputFolder.getAbsolutePath() + File.separator + "out.html"), "UTF-8");
        assertThat(data).startsWith("<!DOCTYPE html>");
        assertThat(data).contains("scopeVariable: test");
        assertThat(data).contains("<script src=\"test.js\"></script>");
    }
}