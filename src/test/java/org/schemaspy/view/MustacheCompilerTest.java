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
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.regex.Pattern;

public class MustacheCompilerTest {

    private MustacheCompiler mustacheCompilerSingle = new MustacheCompiler("testingSingle", "mustache", false);
    private MustacheCompiler mustacheCompilerMulti = new MustacheCompiler("testingMulti", "mustache", true);

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
}