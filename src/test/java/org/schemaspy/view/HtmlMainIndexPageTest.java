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

import org.junit.jupiter.api.Test;
import org.schemaspy.cli.CommandLineArgumentParser;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.model.Catalog;
import org.schemaspy.model.Database;
import org.schemaspy.model.Schema;
import org.schemaspy.util.DataTableConfig;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HtmlMainIndexPageTest {

    @Test
    void descriptionSupportsMarkdown() {
        CommandLineArguments arguments = parse("");
        DataTableConfig dataTableConfig = new DataTableConfig(arguments);
        MustacheCompiler mustacheCompiler = new MustacheCompiler("markdownTest", arguments.getHtmlConfig(), false, dataTableConfig);
        HtmlMainIndexPage htmlMainIndexPage = new HtmlMainIndexPage(mustacheCompiler, "normal *emp* **strong**");
        StringWriter writer = new StringWriter();
        Database database = mock(Database.class);
        when(database.getSchema()).thenReturn(new Schema("schema"));
        when(database.getCatalog()).thenReturn(new Catalog("catalog"));
        htmlMainIndexPage.write(database, Collections.emptyList(), Collections.emptyList(), writer);

        assertThat(writer.toString()).contains("<p>normal <em>emp</em> <strong>strong</strong></p>");
    }

    @Test
    void noRowsTrue_RemovesRowsColumn() {
        CommandLineArguments arguments = parse("-norows");
        DataTableConfig dataTableConfig = new DataTableConfig(arguments);
        MustacheCompiler mustacheCompiler = new MustacheCompiler("noRowsTrue", arguments.getHtmlConfig(), false, dataTableConfig);
        HtmlMainIndexPage htmlMainIndexPage = new HtmlMainIndexPage(mustacheCompiler, null);
        StringWriter writer = new StringWriter();
        Database database = mock(Database.class);
        when(database.getSchema()).thenReturn(new Schema("schema"));
        when(database.getCatalog()).thenReturn(new Catalog("catalog"));
        htmlMainIndexPage.write(database, Collections.emptyList(), Collections.emptyList(), writer);

        assertThat(writer.toString()).doesNotContain("<th align=\"right\" valign=\"bottom\">Rows</th>");
    }

    @Test
    void noRowsFalse_HasRowsColumn() {
        CommandLineArguments arguments = parse("");
        DataTableConfig dataTableConfig = new DataTableConfig(arguments);
        MustacheCompiler mustacheCompiler = new MustacheCompiler("noRowsFalse", arguments.getHtmlConfig(), false, dataTableConfig);
        HtmlMainIndexPage htmlMainIndexPage = new HtmlMainIndexPage(mustacheCompiler, null);
        StringWriter writer = new StringWriter();
        Database database = mock(Database.class);
        when(database.getSchema()).thenReturn(new Schema("schema"));
        when(database.getCatalog()).thenReturn(new Catalog("catalog"));
        htmlMainIndexPage.write(database, Collections.emptyList(), Collections.emptyList(), writer);

        assertThat(writer.toString()).contains("<th align=\"right\" valign=\"bottom\">Rows</th>");
    }

    private CommandLineArguments parse(String...args) {
        String[] defaultArgs = {"-o", "out", "-sso"};
        return new CommandLineArgumentParser(
            new CommandLineArguments(),
            (option) -> null
        )
            .parse(
                Stream
                    .concat(
                        Arrays.stream(defaultArgs),
                        Arrays.stream(args)
                    ).toArray(String[]::new));
    }

}