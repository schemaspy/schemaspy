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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.schemaspy.cli.CommandLineArgumentParser;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.model.ForeignKeyConstraint;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.schemaspy.util.CaseInsensitiveMap;
import org.schemaspy.util.DataTableConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HtmlAnomaliesPageTest {

    private final HtmlConfig htmlConfig = mock(HtmlConfig.class);

    private final CommandLineArguments commandLineArguments = new CommandLineArgumentParser(
            "-o", "out", "-sso"
    )
        .commandLineArguments();

    private final DataTableConfig dataTableConfig = new DataTableConfig(commandLineArguments);
    private final MustacheCompiler mustacheCompiler = new MustacheCompiler("anomalies", "anomalies", commandLineArguments.getHtmlConfig(), false, dataTableConfig);
    private final HtmlAnomaliesPage htmlAnomaliesPage = new HtmlAnomaliesPage(mustacheCompiler);

    @BeforeEach
    void setup() {
        when(htmlConfig.getTemplateDirectory()).thenReturn("layout");
        when(htmlConfig.isPaginationEnabled()).thenReturn(true);
    }
    @Test
    void impliedRelationships() {
        StringWriter output = new StringWriter();

        Table parentTable = mock(Table.class);
        when(parentTable.getName()).thenReturn("parentTable");
        TableColumn parentColumn = new TableColumn(parentTable);
        parentColumn.setName("parentColumn");

        Table childTable = mock(Table.class);
        when(childTable.getName()).thenReturn("childTable");
        TableColumn childColumn = new TableColumn(childTable);
        childColumn.setName("childColumn");

        ForeignKeyConstraint implied = new ForeignKeyConstraint(parentColumn, childColumn);

        htmlAnomaliesPage.write(
                Collections.emptyList(),
                Collections.singletonList(implied),
                output);
        assertThat(output.toString()).contains("<td><a href='tables/childTable.html'>childTable</a>.[childColumn]</td>");
        assertThat(output.toString()).contains("<td><a href='tables/parentTable.html'>parentTable</a>.[parentColumn]</td>");
    }

    @Test
    void tablesWithoutIndex() {
        StringWriter output = new StringWriter();

        Table table = mock(Table.class);
        when(table.getName()).thenReturn("hasNoIndex");
        when(table.getIndexes()).thenReturn(Collections.emptySet());
        when(table.getPrimaryColumns()).thenReturn(Collections.emptyList());

        htmlAnomaliesPage.write(
                Collections.singletonList(table),
                Collections.emptyList(),
                output);
        assertThat(output.toString()).contains("<td><a href='tables/hasNoIndex.html'>hasNoIndex</a></td>");
    }

    @Test
    void tableWithIncrementingColumnNames() {
        StringWriter output = new StringWriter();
        Table table = mock(Table.class);
        when(table.getName()).thenReturn("denormalized");
        CaseInsensitiveMap<TableColumn> columns = new CaseInsensitiveMap<>();
        when(table.getColumnsMap()).thenReturn(columns);
        when(table.getColumns()).thenAnswer((i) -> new ArrayList<>(((Table)i.getMock()).getColumnsMap().values()));

        TableColumn first = new TableColumn(table);
        first.setName("column1");
        table.getColumnsMap().put(first.getName(), first);

        TableColumn second = new TableColumn(table);
        second.setName("column2");
        table.getColumnsMap().put(second.getName(), second);

        TableColumn third = new TableColumn(table);
        third.setName("column3");
        table.getColumnsMap().put(third.getName(), third);

        htmlAnomaliesPage.write(
                Collections.singletonList(table),
                Collections.emptyList(),
                output);
        assertThat(output.toString()).contains("<td><a href='tables/denormalized.html'>denormalized</a></td>");
    }

    @Test
    void tableWithSingleColumn() {
        StringWriter output = new StringWriter();
        Table table = mock(Table.class);
        when(table.getName()).thenReturn("hasOnlyOneColumn");
        CaseInsensitiveMap<TableColumn> columns = new CaseInsensitiveMap<>();
        when(table.getColumnsMap()).thenReturn(columns);
        when(table.getColumns()).thenAnswer((i) -> new ArrayList<>(((Table)i.getMock()).getColumnsMap().values()));
        when(table.isView()).thenReturn(false);

        TableColumn first = new TableColumn(table);
        first.setName("singleColumn");
        table.getColumnsMap().put(first.getName(), first);

        htmlAnomaliesPage.write(
                Collections.singletonList(table),
                Collections.emptyList(),
                output);
        assertThat(output.toString()).contains("<td><a href='tables/hasOnlyOneColumn.html'>hasOnlyOneColumn</a></td>");
        assertThat(output.toString()).contains("<td>singleColumn</td>");
    }

    @Test
    void columnsWithDefaultValueWordNULL() {
        StringWriter output = new StringWriter();
        Table table = mock(Table.class);
        when(table.getName()).thenReturn("defaultNullTable");
        CaseInsensitiveMap<TableColumn> columns = new CaseInsensitiveMap<>();
        when(table.getColumnsMap()).thenReturn(columns);
        when(table.getColumns()).thenAnswer((i) -> new ArrayList<>(((Table)i.getMock()).getColumnsMap().values()));

        TableColumn column = new TableColumn(table);
        column.setName("defaultNullColumn");
        column.setDefaultValue("'null'");
        table.getColumnsMap().put(column.getName(), column);

        htmlAnomaliesPage.write(
                Collections.singletonList(table),
                Collections.emptyList(),
                output);
        assertThat(output.toString()).contains("<td><a href='tables/defaultNullTable.html'>defaultNullTable</a>.defaultNullColumn</td>");
    }

}