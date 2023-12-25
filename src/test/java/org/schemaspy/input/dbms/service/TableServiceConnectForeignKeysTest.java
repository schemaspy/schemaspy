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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.schemaspy.input.dbms.service.helper.RemoteTableIdentifier;
import org.schemaspy.model.*;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TableServiceConnectForeignKeysTest {

    private SqlService sqlService;

    private static final Pattern DEFAULT_COLUMN_EXCLUSION = Pattern.compile("[^.]");
    private static final Pattern DEFAULT_TABLE_INCLUSION = Pattern.compile(".*"); // match everything
    private static final Pattern DEFAULT_TABLE_EXCLUSION = Pattern.compile(".*\\$.*");
    private ColumnService columnService;

    private IndexService indexService;

    private final DbmsMeta dbmsMeta = mock(DbmsMeta.class);

    private Database database;

    private Table table;

    private TableColumn mainPrimary;
    private TableColumn mainForeign;

    @BeforeEach
    public void setup() {
        sqlService = mock(SqlService.class);
        columnService = new ColumnService(sqlService, DEFAULT_COLUMN_EXCLUSION, DEFAULT_COLUMN_EXCLUSION);
        indexService = new IndexService(sqlService, new Properties());
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
    void getImportedKeys() throws SQLException {
        TableService tableService = new TableService(
                sqlService,
                false,
                false,
                DEFAULT_TABLE_INCLUSION,
                DEFAULT_TABLE_EXCLUSION,
                new Properties(),
                columnService,
                indexService
        );

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

        assertThat(table.getMaxChildren()).isZero();
        assertThat(table.getMaxParents()).isZero();
        assertThat(remoteParent.getMaxChildren()).isZero();
        assertThat(remoteParent.getMaxParents()).isZero();

        assertThat(table.getForeignKeys()).isEmpty();
        assertThat(remoteParent.getForeignKeys()).isEmpty();

        assertThat(mainPrimary.getChildren()).isEmpty();
        assertThat(mainPrimary.getParents()).isEmpty();

        assertThat(mainForeign.getChildren()).isEmpty();
        assertThat(mainForeign.getParents()).isEmpty();

        assertThat(parent.getChildren()).isEmpty();
        assertThat(parent.getParents()).isEmpty();

        tableService.connectForeignKeys(database, table, database.getLocals());

        assertThat(table.getMaxChildren()).isZero();
        assertThat(table.getMaxParents()).isEqualTo(1);
        assertThat(remoteParent.getMaxChildren()).isEqualTo(1);
        assertThat(remoteParent.getMaxParents()).isZero();

        assertThat(table.getForeignKeys()).hasSize(1);
        assertThat(remoteParent.getForeignKeys()).isEmpty();

        assertThat(mainPrimary.getChildren()).isEmpty();
        assertThat(mainPrimary.getParents()).isEmpty();

        assertThat(mainForeign.getChildren()).isEmpty();
        assertThat(mainForeign.getParents()).hasSize(1);

        assertThat(parent.getChildren()).hasSize(1);
        assertThat(parent.getParents()).isEmpty();
    }

    @Test
    void getExportedKeys() throws SQLException {
        TableService tableService = new TableService(
                sqlService,
                true,
                false,
                DEFAULT_TABLE_INCLUSION,
                DEFAULT_TABLE_EXCLUSION,
                new Properties(),
                columnService,
                indexService
        );
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

        assertThat(table.getMaxChildren()).isZero();
        assertThat(table.getMaxParents()).isZero();
        assertThat(database.getRemoteTables()).isEmpty();

        assertThat(table.getForeignKeys()).isEmpty();

        assertThat(mainPrimary.getChildren()).isEmpty();
        assertThat(mainPrimary.getParents()).isEmpty();

        assertThat(mainForeign.getChildren()).isEmpty();
        assertThat(mainForeign.getParents()).isEmpty();

        tableService.connectForeignKeys(database, table, database.getLocals());

        assertThat(table.getMaxChildren()).isEqualTo(1);
        assertThat(table.getMaxParents()).isZero();
        assertThat(database.getRemoteTables()).hasSize(1);

        Table remote = database.getRemoteTablesMap().get("child.child.child");
        assertThat(remote).isNotNull();

        TableColumn child = remote.getColumn("child");
        assertThat(child).isNotNull();

        assertThat(table.getForeignKeys()).isEmpty();
        assertThat(remote.getForeignKeys()).hasSize(1);

        assertThat(mainPrimary.getChildren()).hasSize(1);
        assertThat(mainPrimary.getParents()).isEmpty();

        assertThat(mainForeign.getChildren()).isEmpty();
        assertThat(mainForeign.getParents()).isEmpty();

        assertThat(child.getChildren()).isEmpty();
        assertThat(child.getParents()).hasSize(1);
    }

}