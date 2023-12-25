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
package org.schemaspy.input.dbms.service;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.schemaspy.input.dbms.service.helper.RemoteTableIdentifier;
import org.schemaspy.input.dbms.xml.ForeignKeyMeta;
import org.schemaspy.input.dbms.xml.TableColumnMeta;
import org.schemaspy.input.dbms.xml.TableMeta;
import org.schemaspy.model.*;
import org.schemaspy.testing.logback.Logback;
import org.schemaspy.testing.logback.LogbackExtension;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TableServiceConnectTest {

    @RegisterExtension
    public static LogbackExtension logback = new LogbackExtension();

    private final SqlService sqlService = mock(SqlService.class);

    private static final Pattern DEFAULT_COLUMN_EXCLUSION = Pattern.compile("[^.]");
    private final ColumnService columnService = new ColumnService(sqlService, DEFAULT_COLUMN_EXCLUSION, DEFAULT_COLUMN_EXCLUSION);
    private static final Pattern DEFAULT_TABLE_INCLUSION = Pattern.compile(".*"); // match everything
    private static final Pattern DEFAULT_TABLE_EXCLUSION = Pattern.compile(".*\\$.*");

    private final IndexService indexService = new IndexService(sqlService, new Properties());

    private final TableService tableService = new TableService(
            sqlService,
            false,
            false,
            DEFAULT_TABLE_INCLUSION,
            DEFAULT_TABLE_EXCLUSION,
            new Properties(),
            columnService,
            indexService
    );

    private final DbmsMeta dbmsMeta = mock(DbmsMeta.class);

    private Database database;

    private Table table;

    @BeforeEach
    public void setup() {
        database = new Database(dbmsMeta, "tableServiceTest","connect", "tst");
        table = new Table(database, database.getCatalog().getName(), database.getSchema().getName(), "mainTable", "mainTable");
        database.getTablesMap().put(table.getName(), table);
    }

    @Test
    void addRemote() throws SQLException {
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(false);
        when(databaseMetaData.getColumns(anyString(), anyString(), anyString(), anyString())).thenReturn(resultSet);
        when(databaseMetaData.getImportedKeys(anyString(), anyString(), anyString())).thenReturn(resultSet);
        when(sqlService.getDatabaseMetaData()).thenReturn(databaseMetaData);

        ForeignKeyMeta foreignKeyMeta = mock(ForeignKeyMeta.class);
        TableColumnMeta tableColumnMeta = mock(TableColumnMeta.class);
        TableMeta tableMeta = mock(TableMeta.class);

        when(foreignKeyMeta.getRemoteCatalog()).thenReturn("other");
        when(foreignKeyMeta.getRemoteSchema()).thenReturn("other");
        when(foreignKeyMeta.getTableName()).thenReturn("parent");
        when(foreignKeyMeta.getColumnName()).thenReturn("parent");

        when(tableColumnMeta.getName()).thenReturn("child");
        when(tableColumnMeta.getForeignKeys()).thenReturn(Collections.singletonList(foreignKeyMeta));
        when(tableMeta.getColumns()).thenReturn(Collections.singletonList(tableColumnMeta));

        TableColumn childColumn = new TableColumn(table);
        childColumn.setId(0);
        childColumn.setName("child");
        table.getColumnsMap().put(childColumn.getName(), childColumn);

        tableService.connect(database, table, tableMeta ,database.getLocals());

        assertThat(database.getRemoteTablesMap().get("other.other.parent")).isNotNull();
        assertThat(database.getRemoteTablesMap().get("other.other.parent").getColumn("parent")).isNotNull();
    }

    @Test
    @Logback(TableService.class)
    void remoteExists() throws SQLException {
        logback.expect(Matchers.containsString("Assuming 'parent.parent' is a primary key due to being referenced by 'mainTable.child'"));
        ForeignKeyMeta foreignKeyMeta = mock(ForeignKeyMeta.class);
        TableColumnMeta tableColumnMeta = mock(TableColumnMeta.class);
        TableMeta tableMeta = mock(TableMeta.class);

        when(foreignKeyMeta.getRemoteCatalog()).thenReturn("other");
        when(foreignKeyMeta.getRemoteSchema()).thenReturn("other");
        when(foreignKeyMeta.getTableName()).thenReturn("parent");
        when(foreignKeyMeta.getColumnName()).thenReturn("parent");

        when(tableColumnMeta.getName()).thenReturn("child");
        when(tableColumnMeta.getForeignKeys()).thenReturn(Collections.singletonList(foreignKeyMeta));
        when(tableMeta.getColumns()).thenReturn(Collections.singletonList(tableColumnMeta));

        TableColumn childColumn = new TableColumn(table);
        childColumn.setId(0);
        childColumn.setName("child");
        table.getColumnsMap().put(childColumn.getName(), childColumn);

        LogicalRemoteTable logicalRemoteTable = new LogicalRemoteTable(database, new RemoteTableIdentifier("other", "other", "parent"), "tst");
        TableColumn parentColumn = new TableColumn(logicalRemoteTable);
        parentColumn.setName("parent");
        logicalRemoteTable.getColumnsMap().put(parentColumn.getName(), parentColumn);
        database.getRemoteTablesMap().put("other.other.parent", logicalRemoteTable);

        assertThat(childColumn.getChildren().size()).isEqualTo(0);
        assertThat(childColumn.getParents().size()).isEqualTo(0);
        assertThat(parentColumn.getChildren().size()).isEqualTo(0);
        assertThat(parentColumn.getParents().size()).isEqualTo(0);

        assertThat(table.getMaxChildren()).isEqualTo(0);
        assertThat(table.getMaxParents()).isEqualTo(0);
        assertThat(logicalRemoteTable.getMaxChildren()).isEqualTo(0);
        assertThat(logicalRemoteTable.getMaxParents()).isEqualTo(0);

        tableService.connect(database, table, tableMeta ,database.getLocals());

        assertThat(childColumn.getChildren().size()).isEqualTo(0);
        assertThat(childColumn.getParents().size()).isEqualTo(1);
        assertThat(parentColumn.getChildren().size()).isEqualTo(1);
        assertThat(parentColumn.getParents().size()).isEqualTo(0);

        assertThat(table.getMaxChildren()).isEqualTo(0);
        assertThat(table.getMaxParents()).isEqualTo(1);
        assertThat(logicalRemoteTable.getMaxChildren()).isEqualTo(1);
        assertThat(logicalRemoteTable.getMaxParents()).isEqualTo(0);
    }

}