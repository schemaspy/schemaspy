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
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.model.ForeignKeyConstraint;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.schemaspy.util.CaseInsensitiveMap;
import org.schemaspy.util.DataTableConfig;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HtmlAnomaliesPageTest {

    private static HtmlConfig htmlConfig = mock(HtmlConfig.class);
    static {
        when(htmlConfig.getTemplateDirectory()).thenReturn("layout");
        when(htmlConfig.isPaginationEnabled()).thenReturn(true);
    }

    private static DataTableConfig dataTableConfig = new DataTableConfig(htmlConfig, new CommandLineArguments());
    private static MustacheCompiler mustacheCompiler = new MustacheCompiler("anomalies", false, htmlConfig, dataTableConfig);
    private static HtmlAnomaliesPage htmlAnomaliesPage = new HtmlAnomaliesPage(mustacheCompiler);

    @Test
    public void impliedRelationships() {
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
                Collections.EMPTY_LIST,
                Collections.singletonList(implied),
                output);
        assertThat(output.toString()).contains("<td><a href='tables/childTable.html'>childTable</a>.[childColumn]</td>");
        assertThat(output.toString()).contains("<td><a href='tables/parentTable.html'>parentTable</a>.[parentColumn]</td>");
    }

    @Test
    public void tablesWithoutIndex() {
        StringWriter output = new StringWriter();

        Table table = mock(Table.class);
        when(table.getName()).thenReturn("hasNoIndex");
        when(table.getIndexes()).thenReturn(Collections.emptySet());
        when(table.getPrimaryColumns()).thenReturn(Collections.emptyList());

        htmlAnomaliesPage.write(
                Collections.singletonList(table),
                Collections.EMPTY_LIST,
                output);
        assertThat(output.toString()).contains("<td><a href='tables/hasNoIndex.html'>hasNoIndex</a></td>");
    }

    @Test
    public void tableWithIncrementingColumnNames() {
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
                Collections.EMPTY_LIST,
                output);
        assertThat(output.toString()).contains("<td><a href='tables/denormalized.html'>denormalized</a></td>");
    }

    @Test
    public void tableWithSingleColumn() {
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
                Collections.EMPTY_LIST,
                output);
        assertThat(output.toString()).contains("<td><a href='tables/hasOnlyOneColumn.html'>hasOnlyOneColumn</a></td>");
        assertThat(output.toString()).contains("<td>singleColumn</td>");
    }

    @Test
    public void columnsWithDefaultValueWordNULL() {
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
                Collections.EMPTY_LIST,
                output);
        assertThat(output.toString()).contains("<td><a href='tables/defaultNullTable.html'>defaultNullTable</a>.defaultNullColumn</td>");
    }

}