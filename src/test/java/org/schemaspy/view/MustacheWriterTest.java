package org.schemaspy.view;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

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