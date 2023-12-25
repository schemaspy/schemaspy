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
import org.schemaspy.input.dbms.service.helper.ImportForeignKey;
import org.schemaspy.model.*;
import org.schemaspy.testing.logback.Logback;
import org.schemaspy.testing.logback.LogbackExtension;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TableServiceAddForeignKeyTest {

    @RegisterExtension
    public static LogbackExtension logback = new LogbackExtension();

    private final SqlService sqlService = mock(SqlService.class);

    private static final Pattern DEFAULT_COLUMN_EXCLUSION = Pattern.compile("[^.]");
    private static final Pattern DEFAULT_TABLE_INCLUSION = Pattern.compile(".*"); // match everything
    private static final Pattern DEFAULT_TABLE_EXCLUSION = Pattern.compile(".*\\$.*");

    private final ColumnService columnService = new ColumnService(sqlService, DEFAULT_COLUMN_EXCLUSION, DEFAULT_COLUMN_EXCLUSION);

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
        database = new Database(dbmsMeta, "tableServiceTest","addFK", "tst");
        table = new Table(database, database.getCatalog().getName(), database.getSchema().getName(), "mainTable", "mainTable");
        database.getTablesMap().put(table.getName(), table);
    }
    
    @Test
    @Logback(value = TableService.class, level = "debug")
    void excludingTable() throws SQLException {
        logback.expect(Matchers.containsString("Ignoring CAT.S.excludeMePlease referenced by FK notNull"));
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
        new TableService(
                sqlService,
                false,
                false,
                DEFAULT_TABLE_INCLUSION,
                Pattern.compile("excludeMePlease"),
                new Properties(),
                columnService,
                indexService
        ).addForeignKey(database, null, foreignKey, new HashMap<>());
    }

    @Test
    @Logback(TableService.class)
    void addsForeignKeyIfMissing() throws SQLException {
        logback.expect(Matchers.containsString("Couldn't add FK 'newFK' to table 'mainTable' - Column 'fkColumn' doesn't exist"));
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
    }

    @Test
    @Logback(TableService.class)
    void usesExistingForeignKeyIfExists() throws SQLException {
        logback.expect(Matchers.containsString("Couldn't add FK 'existingFK' to table 'mainTable' - Column 'fkColumn' doesn't exist"));
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
    }

    @Test
    @Logback(value = TableService.class, level = "debug")
    void addingRemoteTable() throws SQLException {
        logback.expect(Matchers.containsString("Adding remote table other.other.parent"));
        logback.expect(Matchers.containsString("Couldn't add FK 'withChild' to table 'mainTable' - Column 'parent' doesn't exist in table 'parent'"));
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
        assertThat(database.getRemoteTablesMap().get("other.other.parent")).isNotNull();
        assertThat(table.getForeignKeysMap().get("withChild")).isNotNull();
    }

    @Test
    void addedForeignKeyAndWiring() throws SQLException {
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

        assertThat(childColumn.getChildren()).isEmpty();
        assertThat(childColumn.getParents()).isEmpty();
        assertThat(parentColumn.getChildren()).isEmpty();
        assertThat(parentColumn.getParents()).isEmpty();

        assertThat(table.getMaxChildren()).isZero();
        assertThat(table.getMaxParents()).isZero();
        assertThat(remoteTable.getMaxChildren()).isZero();
        assertThat(remoteTable.getMaxParents()).isZero();

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

        assertThat(childColumn.getChildren()).isEmpty();
        assertThat(childColumn.getParents()).hasSize(1);
        assertThat(parentColumn.getChildren()).hasSize(1);
        assertThat(parentColumn.getParents()).isEmpty();

        assertThat(table.getMaxChildren()).isZero();
        assertThat(table.getMaxParents()).isEqualTo(1);
        assertThat(remoteTable.getMaxChildren()).isEqualTo(1);
        assertThat(remoteTable.getMaxParents()).isZero();
    }

}