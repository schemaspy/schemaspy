/*
 * Copyright (C) 2019 Nils Petzaell
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

import org.junit.Test;
import org.schemaspy.cli.CommandLineArgumentParser;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.model.Table;
import org.schemaspy.util.DataTableConfig;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HtmlTablePageTest {

    @Test
    public void noRowsFalse_showNoRows() {
        CommandLineArguments arguments = parse("");
        DataTableConfig dataTableConfig = new DataTableConfig(arguments);
        MustacheCompiler mustacheCompiler = new MustacheCompiler("table_noRowsFalse", "table_noRowsFalse", arguments.getHtmlConfig(), false, dataTableConfig);
        HtmlTablePage htmlTablePage = new HtmlTablePage(mustacheCompiler, null);
        StringWriter writer = new StringWriter();

        Table table = mock(Table.class);
        when(table.getName()).thenReturn("A_TABLE");
        when(table.getNumRows()).thenReturn(0L);

        htmlTablePage.write(table, Collections.emptyList(), writer);

        assertThat(writer.toString()).contains("<h1>A_TABLE</h1><p><span id=\"recordNumber\">0</span> rows</p><br />");
    }

    @Test
    public void noRowsTrue_hideNoRows() {
        CommandLineArguments arguments = parse("-norows");
        DataTableConfig dataTableConfig = new DataTableConfig(arguments);
        MustacheCompiler mustacheCompiler = new MustacheCompiler("table_noRowsFalse", "table_noRowsFalse", arguments.getHtmlConfig(), false, dataTableConfig);
        HtmlTablePage htmlTablePage = new HtmlTablePage(mustacheCompiler, null);
        StringWriter writer = new StringWriter();

        Table table = mock(Table.class);
        when(table.getName()).thenReturn("A_TABLE");
        when(table.getNumRows()).thenReturn(0L);

        htmlTablePage.write(table, Collections.emptyList(), writer);

        assertThat(writer.toString()).doesNotContain("<h1>A_TABLE</h1><p><span id=\"recordNumber\">0</span> rows</p><br />");
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