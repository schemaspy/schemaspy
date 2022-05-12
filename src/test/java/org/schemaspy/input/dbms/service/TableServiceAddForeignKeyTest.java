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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.schemaspy.Config;
import org.schemaspy.input.dbms.service.helper.ImportForeignKey;
import org.schemaspy.model.*;
import org.schemaspy.testing.ConfigRule;
import org.schemaspy.testing.Logger;
import org.schemaspy.testing.LoggingRule;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TableServiceAddForeignKeyTest {

    @Rule
    public LoggingRule loggingRule = new LoggingRule();

    @Rule
    public ConfigRule configRule = new ConfigRule();

    private SqlService sqlService = mock(SqlService.class);

    private static final Pattern DEFAULT_COLUMN_EXCLUSION = Pattern.compile("[^.]");

    private ColumnService columnService = new ColumnService(sqlService, DEFAULT_COLUMN_EXCLUSION, DEFAULT_COLUMN_EXCLUSION);

    private IndexService indexService = new IndexService(sqlService);

    private TableService tableService = new TableService(sqlService, columnService, indexService);

    private DbmsMeta dbmsMeta = mock(DbmsMeta.class);

    private Database database;

    private Table table;

    @Before
    public void setup() {
        database = new Database(dbmsMeta, "tableServiceTest","addFK", "tst");
        table = new Table(database, database.getCatalog().getName(), database.getSchema().getName(), "mainTable", "mainTable");
        database.getTablesMap().put(table.getName(), table);
    }
    
    @Test
    @Logger(value = TableService.class, level = "debug")
    public void excludingTable() throws SQLException {
        Config.getInstance().setTableExclusions("excludeMePlease");
        ImportForeignKey foreignKey = new ImportForeignKey.Builder()
                .withFkName("notNull")
                .withFkColumnName(null)
                .withPkTableCat("CAT")
                .withPkTableSchema("S")
                .withPkTableName("excludeMePlease")
                .withPkColumnName("aColumn")
                .withUpdateRule(0)
                .withDeleteRule(0)
                .build();
        tableService.addForeignKey(database, null, foreignKey, new HashMap<>());
        assertThat(loggingRule.getLog()).contains("Ignoring CAT.S.excludeMePlease referenced by FK notNull");
    }

    @Test
    @Logger(TableService.class)
    public void addsForeignKeyIfMissing() throws SQLException {
        ImportForeignKey foreignKey = new ImportForeignKey.Builder()
                .withFkName("newFK")
                .withFkColumnName("fkColumn")
                .withPkTableCat("pkCat")
                .withPkTableSchema("pkSchema")
                .withPkTableName("pkTable")
                .withPkColumnName("pkColumn")
                .withUpdateRule(0)
                .withDeleteRule(0)
                .build();
        tableService.addForeignKey(database, table, foreignKey, database.getTablesMap());
        assertThat(table.getForeignKeysMap().get("newFK")).isNotNull();
        assertThat(table.getForeignKeysMap().get("newFK").getName()).isEqualTo("newFK");
        assertThat(loggingRule.getLog()).contains("Couldn't add FK 'newFK' to table 'mainTable' - Column 'fkColumn' doesn't exist");
    }

    @Test
    @Logger(TableService.class)
    public void usesExistingForeignKeyIfExists() throws SQLException {
        table.getForeignKeysMap().put("existingFK", new ForeignKeyConstraint(table, "existingFK", 1,1));
        ImportForeignKey foreignKey = new ImportForeignKey.Builder()
                .withFkName("existingFK")
                .withFkColumnName("fkColumn")
                .withPkTableCat("pkCat")
                .withPkTableSchema("pkSchema")
                .withPkTableName("pkTable")
                .withPkColumnName("pkColumn")
                .withUpdateRule(0)
                .withDeleteRule(0)
                .build();
        tableService.addForeignKey(database, table, foreignKey, new HashMap<>());
        assertThat(table.getForeignKeysMap().get("existingFK").getUpdateRule()).isEqualTo(1);
        assertThat(table.getForeignKeysMap().get("existingFK").getDeleteRule()).isEqualTo(1);
        assertThat(loggingRule.getLog()).contains("Couldn't add FK 'existingFK' to table 'mainTable' - Column 'fkColumn' doesn't exist");
    }

    @Test
    @Logger(value = TableService.class, level = "debug")
    public void addingRemoteTable() throws SQLException {
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(false);
        when(databaseMetaData.getColumns(anyString(), anyString(), anyString(), anyString())).thenReturn(resultSet);
        when(databaseMetaData.getImportedKeys(anyString(), anyString(), anyString())).thenReturn(resultSet);
        when(sqlService.getDatabaseMetaData()).thenReturn(databaseMetaData);
        TableColumn childColumn = new TableColumn(table);
        childColumn.setId(0);
        childColumn.setName("childColumn");
        table.getColumnsMap().put(childColumn.getName(), childColumn);
        ImportForeignKey foreignKey = new ImportForeignKey.Builder()
                .withFkName("withChild")
                .withFkColumnName("childColumn")
                .withPkTableCat("other")
                .withPkTableSchema("other")
                .withPkTableName("parent")
                .withPkColumnName("parent")
                .withUpdateRule(0)
                .withDeleteRule(0)
                .build();
        tableService.addForeignKey(database, table, foreignKey, database.getTablesMap());
        String log = loggingRule.getLog();
        assertThat(log).contains("Adding remote table other.other.parent");
        assertThat(log).contains("Couldn't add FK 'withChild' to table 'mainTable' - Column 'parent' doesn't exist in table 'parent'");
        assertThat(database.getRemoteTablesMap().get("other.other.parent")).isNotNull();
        assertThat(table.getForeignKeysMap().get("withChild")).isNotNull();
    }

    @Test
    public void addedForeignKeyAndWiring() throws SQLException {
        TableColumn childColumn = new TableColumn(table);
        childColumn.setId(0);
        childColumn.setName("childColumn");
        table.getColumnsMap().put(childColumn.getName(), childColumn);
        RemoteTable remoteTable = new RemoteTable(database, "other", "other", "parent", "tst");
        database.getRemoteTablesMap().clear();
        database.getRemoteTablesMap().put("other.other.parent", remoteTable);
        TableColumn parentColumn = new TableColumn(remoteTable);
        parentColumn.setId(0);
        parentColumn.setName("parent");
        remoteTable.getColumnsMap().put(parentColumn.getName(), parentColumn);

        assertThat(childColumn.getChildren().size()).isEqualTo(0);
        assertThat(childColumn.getParents().size()).isEqualTo(0);
        assertThat(parentColumn.getChildren().size()).isEqualTo(0);
        assertThat(parentColumn.getParents().size()).isEqualTo(0);

        assertThat(table.getMaxChildren()).isEqualTo(0);
        assertThat(table.getMaxParents()).isEqualTo(0);
        assertThat(remoteTable.getMaxChildren()).isEqualTo(0);
        assertThat(remoteTable.getMaxParents()).isEqualTo(0);

        ImportForeignKey foreignKey = new ImportForeignKey.Builder()
                .withFkName("withChild")
                .withFkColumnName("childColumn")
                .withPkTableCat("other")
                .withPkTableSchema("other")
                .withPkTableName("parent")
                .withPkColumnName("parent")
                .withUpdateRule(0)
                .withDeleteRule(0)
                .build();
        tableService.addForeignKey(database, table, foreignKey, database.getTablesMap());

        assertThat(childColumn.getChildren().size()).isEqualTo(0);
        assertThat(childColumn.getParents().size()).isEqualTo(1);
        assertThat(parentColumn.getChildren().size()).isEqualTo(1);
        assertThat(parentColumn.getParents().size()).isEqualTo(0);

        assertThat(table.getMaxChildren()).isEqualTo(0);
        assertThat(table.getMaxParents()).isEqualTo(1);
        assertThat(remoteTable.getMaxChildren()).isEqualTo(1);
        assertThat(remoteTable.getMaxParents()).isEqualTo(0);

    }

}