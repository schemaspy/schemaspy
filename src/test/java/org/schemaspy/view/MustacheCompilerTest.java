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
package org.schemaspy.view;

import org.assertj.core.api.SoftAssertions;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MustacheCompilerTest {

    private static MustacheCompiler mustacheCompilerSingle;
    private static MustacheCompiler mustacheCompilerMulti;

    @BeforeClass
    public static void setup() {
        HtmlConfig single = mock(HtmlConfig.class);
        when(single.getTemplateDirectory()).thenReturn("mustache");
        when(single.isOneOfMultipleSchemas()).thenReturn(false);
        mustacheCompilerSingle = new MustacheCompiler("testingSingle", single);

        HtmlConfig multi = mock(HtmlConfig.class);
        when(multi.getTemplateDirectory()).thenReturn("mustache");
        when(multi.isOneOfMultipleSchemas()).thenReturn(true);
        mustacheCompilerMulti = new MustacheCompiler("testingMulti", multi);
    }

    private PageData pageData = new PageData.Builder()
            .templateName("databaseName.html")
            .getPageData();

    @Test
    public void setsDatabaseName() throws IOException {
        StringWriter stringWriterSingle = new StringWriter();
        StringWriter stringWriterMulti = new StringWriter();
        mustacheCompilerSingle.write(pageData, stringWriterSingle);
        mustacheCompilerMulti.write(pageData, stringWriterMulti);

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(stringWriterSingle.toString()).contains("databaseName=testingSingle");
        softAssertions.assertThat(stringWriterMulti.toString()).contains("databaseName=testingMulti");
        softAssertions.assertAll();
    }

    @Test
    public void setsRootPath() throws IOException {
        StringWriter stringWriterSingle = new StringWriter();
        StringWriter stringWriterMulti = new StringWriter();
        mustacheCompilerSingle.write(pageData, stringWriterSingle);
        mustacheCompilerMulti.write(pageData, stringWriterMulti);

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(stringWriterSingle.toString()).matches(string->
            Pattern.compile("^rootPath=$", Pattern.MULTILINE).matcher(string).find()
        );
        softAssertions.assertThat(stringWriterMulti.toString()).matches(string ->
            Pattern.compile("^rootPath=$", Pattern.MULTILINE).matcher(string).find()
        );
        softAssertions.assertAll();
    }

    @Test
    public void setsRootPathToHome() throws IOException {
        StringWriter stringWriterSingle = new StringWriter();
        StringWriter stringWriterMulti = new StringWriter();
        mustacheCompilerSingle.write(pageData, stringWriterSingle);
        mustacheCompilerMulti.write(pageData, stringWriterMulti);

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(stringWriterSingle.toString()).matches(string->
                Pattern.compile("^rootPathToHome=$", Pattern.MULTILINE).matcher(string).find()
        );
        softAssertions.assertThat(stringWriterMulti.toString()).matches(string ->
                Pattern.compile("^rootPathToHome=\\.\\./$", Pattern.MULTILINE).matcher(string).find()
        );
        softAssertions.assertAll();
    }

    @Test
    public void overrideLayoutTest() throws IOException {
        Path overridePath = Paths.get("target","override.html");
        HtmlConfig htmlConfig = mock(HtmlConfig.class);
        when(htmlConfig.getTemplateDirectory()).thenReturn("target");
        MustacheCompiler mustacheCompiler = new MustacheCompiler("Override", htmlConfig);
        Files.deleteIfExists(overridePath);
        String before = write(mustacheCompiler);
        assertThat(before).isEqualTo("normal");
        Files.write(overridePath, "custom".getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        //Create new MustacheCompiler this is to evict cache, else the already processed override.html will be used
        mustacheCompiler = new MustacheCompiler("Override", htmlConfig);
        String after = write(mustacheCompiler);
        assertThat(after).isEqualTo("custom");
    }

    private String write(MustacheCompiler mustacheCompiler) throws IOException {
        StringWriter writer = new StringWriter();
        PageData pageData = new PageData.Builder()
                .templateName("override.html")
                .getPageData();
        mustacheCompiler.write(pageData, writer);
        return writer.toString();
    }
}