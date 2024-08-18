/*
 * Copyright (C) 2017, 2018 Nils Petzaell
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
package org.schemaspy.integrationtesting.oracle;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.schemaspy.integrationtesting.OracleSuite;
import org.schemaspy.model.Database;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableIndex;
import org.schemaspy.testing.testcontainers.SuiteContainerExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.schemaspy.testing.DatabaseFixture.database;

/**
 * @author Nils Petzaell
 */
@DisabledOnOs(value = OS.MAC, architectures = {"aarch64"})
class OraclePKIT {

    private static final Path outputPath = Paths.get("target","testout","integrationtesting","oracle","pk");

    private static Database database;

    @RegisterExtension
    static SuiteContainerExtension container = OracleSuite.SUITE_CONTAINER;

    @BeforeAll
    static void gatheringSchemaDetailsTest() throws SQLException, IOException {
        String[] args = {
                "-t", "orathin",
                "-db", "xe",
                "-s", "DBUSER",
                "-cat", "%",
                "-o", outputPath.toString(),
                "-u", "dbuser",
                "-p", "dbuser123",
                "-host", container.getHost(),
                "-port", container.getPort(1521)
        };
        database = database(args);
    }

    @Test
    void databaseShouldHaveTable() {
        Table table = getTable("GIWSDURATIONHISTORY");
        assertThat(table).isNotNull();
    }

    @Test
    void tableShouldHavePKIndex() {
        Table table = getTable("GIWSDURATIONHISTORY");
        assertThat(table.getIndexes().stream().filter(TableIndex::isPrimaryKey).count()).isEqualTo(1);
    }

    private Table getTable(String tableName) {
        return database.getTablesMap().get(tableName);
    }
}
