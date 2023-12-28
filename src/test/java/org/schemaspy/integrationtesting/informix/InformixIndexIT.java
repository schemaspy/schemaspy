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
package org.schemaspy.integrationtesting.informix;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.schemaspy.integrationtesting.InformixSuite;
import org.schemaspy.model.Database;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.schemaspy.model.TableIndex;
import org.schemaspy.testing.SuiteContainerExtension;

import java.io.IOException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.schemaspy.testing.DatabaseFixture.database;

/**
 * @author Nils Petzaell
 */
@DisabledOnOs(value = OS.MAC, architectures = {"aarch64"})
class InformixIndexIT {

    private static Database database;

    @RegisterExtension
    static SuiteContainerExtension container = InformixSuite.SUITE_CONTAINER;

    @BeforeAll
    static void gatheringSchemaDetailsTest() throws SQLException, IOException {
        String[] args = {
                "-t", "informix",
                "-db", "test",
                "-s", "informix",
                "-cat", "test",
                "-server", "dev",
                "-o", "target/testout/integrationtesting/informix/index",
                "-u", container.getUsername(),
                "-p", container.getPassword(),
                "-host", container.getHost(),
                "-port", container.getPort(9088)
        };
        database = database(args);
    }

    @Test
    void databaseShouldBePopulatedWithTableTest() {
        Table table = getTable("test");
        assertThat(table).isNotNull();
    }

    @Test
    void databaseShouldBePopulatedWithTableTestAndHaveColumnName() {
        Table table = getTable("test");
        TableColumn column = table.getColumn("firstname");
        assertThat(column).isNotNull();
    }

    @Test
    void tableTestShouldHaveTwoIndexes() {
        Table table = getTable("test");
        assertThat(table.getIndexes()).hasSize(2);
    }

    @Test
    void tableTestIndex_test_index_shouldHaveThreeColumns() {
        TableIndex index = getTable("test").getIndex("test_index");
        assertThat(index.getColumns()).hasSize(3);
    }

    private Table getTable(String tableName) {
        return database.getTablesMap().get(tableName);
    }
}
