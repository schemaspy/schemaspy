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
package org.schemaspy.service;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.schemaspy.Config;
import org.schemaspy.model.*;
import org.schemaspy.service.helper.RemoteTableIdentifier;
import org.schemaspy.testing.ConfigRule;
import org.schemaspy.testing.LoggingRule;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TableServiceConnectForeignKeysTest {

    @Rule
    public LoggingRule loggingRule = new LoggingRule();

    @Rule
    public ConfigRule configRule = new ConfigRule();

    private SqlService sqlService;

    private TableService tableService;

    private DbmsMeta dbmsMeta = mock(DbmsMeta.class);

    private Database database;

    private Table table;

    private TableColumn mainPrimary;
    private TableColumn mainForeign;

    @Before
    public void setup() {
        sqlService = mock(SqlService.class);
        tableService = new TableService(sqlService);
        database = new Database(dbmsMeta, "tableServiceTest","connectFK", "tst");
        table = new Table(database, database.getCatalog().getName(), database.getSchema().getName(), "mainTable", "mainTable");
        database.getTablesMap().put(table.getName(), table);

        mainPrimary = new TableColumn(table);
        mainPrimary.setName("mainPrimary");
        table.getColumnsMap().put(mainPrimary.getName(), mainPrimary);
        table.setPrimaryColumn(mainPrimary);

        mainForeign = new TableColumn(table);
        mainForeign.setName("mainForeign");
        table.getColumnsMap().put(mainForeign.getName(), mainForeign);
    }

    @Test
    public void getImportedKeys() throws SQLException {
        new Config("-noexportedkeys");

        ResultSet importKeysResultSet = mock(ResultSet.class);
        when(importKeysResultSet.next()).thenReturn(true, false);
        when(importKeysResultSet.getString("FK_NAME")).thenReturn("main_parent");
        when(importKeysResultSet.getString("FKCOLUMN_NAME")).thenReturn("mainForeign");
        when(importKeysResultSet.getString("PKTABLE_CAT")).thenReturn("parent");
        when(importKeysResultSet.getString("PKTABLE_SCHEM")).thenReturn("parent");
        when(importKeysResultSet.getString("PKTABLE_NAME")).thenReturn("parent");
        when(importKeysResultSet.getString("PKCOLUMN_NAME")).thenReturn("parent");
        when(importKeysResultSet.getInt("UPDATE_RULE")).thenReturn(0);
        when(importKeysResultSet.getInt("DELETE_RULE")).thenReturn(0);

        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(databaseMetaData.getImportedKeys("connectFK", "tst", "mainTable")).thenReturn(importKeysResultSet);

        when(sqlService.getDatabaseMetaData()).thenReturn(databaseMetaData);

        LogicalRemoteTable remoteParent = new LogicalRemoteTable(database, new RemoteTableIdentifier("parent", "parent", "parent"), "tst");
        TableColumn parent = new TableColumn(remoteParent);
        parent.setName("parent");
        remoteParent.getColumnsMap().put(parent.getName(), parent);
        database.getRemoteTablesMap().put(database.getRemoteTableKey(remoteParent.getCatalog(), remoteParent.getSchema(),remoteParent.getName()), remoteParent);

        assertThat(table.getMaxChildren()).isEqualTo(0);
        assertThat(table.getMaxParents()).isEqualTo(0);
        assertThat(remoteParent.getMaxChildren()).isEqualTo(0);
        assertThat(remoteParent.getMaxParents()).isEqualTo(0);

        assertThat(table.getForeignKeys().size()).isEqualTo(0);
        assertThat(remoteParent.getForeignKeys().size()).isEqualTo(0);

        assertThat(mainPrimary.getChildren().size()).isEqualTo(0);
        assertThat(mainPrimary.getParents().size()).isEqualTo(0);

        assertThat(mainForeign.getChildren().size()).isEqualTo(0);
        assertThat(mainForeign.getParents().size()).isEqualTo(0);

        assertThat(parent.getChildren().size()).isEqualTo(0);
        assertThat(parent.getParents().size()).isEqualTo(0);

        tableService.connectForeignKeys(database, table, database.getLocals());

        assertThat(table.getMaxChildren()).isEqualTo(0);
        assertThat(table.getMaxParents()).isEqualTo(1);
        assertThat(remoteParent.getMaxChildren()).isEqualTo(1);
        assertThat(remoteParent.getMaxParents()).isEqualTo(0);

        assertThat(table.getForeignKeys().size()).isEqualTo(1);
        assertThat(remoteParent.getForeignKeys().size()).isEqualTo(0);

        assertThat(mainPrimary.getChildren().size()).isEqualTo(0);
        assertThat(mainPrimary.getParents().size()).isEqualTo(0);

        assertThat(mainForeign.getChildren().size()).isEqualTo(0);
        assertThat(mainForeign.getParents().size()).isEqualTo(1);

        assertThat(parent.getChildren().size()).isEqualTo(1);
        assertThat(parent.getParents().size()).isEqualTo(0);
    }

    @Test
    public void getExportedKeys() throws SQLException {
        ResultSet importKeysResultSet = mock(ResultSet.class);
        when(importKeysResultSet.next()).thenReturn(false);

        ResultSet exportedKeysResultSet = mock(ResultSet.class);
        when(exportedKeysResultSet.next()).thenReturn(true, false);
        when(exportedKeysResultSet.getString("FKTABLE_CAT")).thenReturn("child");
        when(exportedKeysResultSet.getString("FKTABLE_SCHEM")).thenReturn("child");
        when(exportedKeysResultSet.getString("FKTABLE_NAME")).thenReturn("child");

        ResultSet childColumn = mock(ResultSet.class);
        when(childColumn.next()).thenReturn(true, false);
        when(childColumn.getString("COLUMN_NAME")).thenReturn("child");
        when(childColumn.getString("TYPE_NAME")).thenReturn("int");
        when(childColumn.getInt("DATA_TYPE")).thenReturn(0);
        when(childColumn.getInt("DECIMAL_DIGITS")).thenReturn(0);
        when(childColumn.getObject("BUFFER_LENGTH")).thenReturn(null);
        when(childColumn.getInt("COLUMN_SIZE")).thenReturn(10);
        when(childColumn.getInt("NULLABLE")).thenReturn(1);
        when(childColumn.getString("COLUMN_DEF")).thenReturn("0");
        when(childColumn.getString("REMARKS")).thenReturn("none");
        when(childColumn.getInt("ORDINAL_POSITION")).thenReturn(2);

        ResultSet importedKeysResultSetForChild = mock(ResultSet.class);
        when(importedKeysResultSetForChild.next()).thenReturn(true, false);
        when(importedKeysResultSetForChild.getString("FK_NAME")).thenReturn("child_main");
        when(importedKeysResultSetForChild.getString("FKCOLUMN_NAME")).thenReturn("child");
        when(importedKeysResultSetForChild.getString("PKTABLE_CAT")).thenReturn("connectFK");
        when(importedKeysResultSetForChild.getString("PKTABLE_SCHEM")).thenReturn("tst");
        when(importedKeysResultSetForChild.getString("PKTABLE_NAME")).thenReturn("mainTable");
        when(importedKeysResultSetForChild.getString("PKCOLUMN_NAME")).thenReturn("mainPrimary");
        when(importedKeysResultSetForChild.getInt("UPDATE_RULE")).thenReturn(0);
        when(importedKeysResultSetForChild.getInt("DELETE_RULE")).thenReturn(0);

        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(databaseMetaData.getImportedKeys("connectFK", "tst", "mainTable")).thenReturn(importKeysResultSet);
        when(databaseMetaData.getExportedKeys("connectFK", "tst", "mainTable")).thenReturn(exportedKeysResultSet);
        when(databaseMetaData.getColumns("child", "child", "child", "%")).thenReturn(childColumn);
        when(databaseMetaData.getImportedKeys("child","child", "child")).thenReturn(importedKeysResultSetForChild);

        when(sqlService.getDatabaseMetaData()).thenReturn(databaseMetaData);

        assertThat(table.getMaxChildren()).isEqualTo(0);
        assertThat(table.getMaxParents()).isEqualTo(0);
        assertThat(database.getRemoteTables().size()).isEqualTo(0);

        assertThat(table.getForeignKeys().size()).isEqualTo(0);

        assertThat(mainPrimary.getChildren().size()).isEqualTo(0);
        assertThat(mainPrimary.getParents().size()).isEqualTo(0);

        assertThat(mainForeign.getChildren().size()).isEqualTo(0);
        assertThat(mainForeign.getParents().size()).isEqualTo(0);

        tableService.connectForeignKeys(database, table, database.getLocals());

        assertThat(table.getMaxChildren()).isEqualTo(1);
        assertThat(table.getMaxParents()).isEqualTo(0);
        assertThat(database.getRemoteTables().size()).isEqualTo(1);

        Table remote = database.getRemoteTablesMap().get("child.child.child");
        assertThat(remote).isNotNull();

        TableColumn child = remote.getColumn("child");
        assertThat(child).isNotNull();

        assertThat(table.getForeignKeys().size()).isEqualTo(0);
        assertThat(remote.getForeignKeys().size()).isEqualTo(1);

        assertThat(mainPrimary.getChildren().size()).isEqualTo(1);
        assertThat(mainPrimary.getParents().size()).isEqualTo(0);

        assertThat(mainForeign.getChildren().size()).isEqualTo(0);
        assertThat(mainForeign.getParents().size()).isEqualTo(0);

        assertThat(child.getChildren().size()).isEqualTo(0);
        assertThat(child.getParents().size()).isEqualTo(1);

    }

}