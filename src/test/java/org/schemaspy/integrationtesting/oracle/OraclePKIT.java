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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.schemaspy.model.Database;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableIndex;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.script.ScriptException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.schemaspy.testing.DatabaseFixture.database;

/**
 * @author Nils Petzaell
 */
@DisabledOnOs(value = OS.MAC, architectures = {"aarch64"})
@Testcontainers(disabledWithoutDocker = true)
public class OraclePKIT {

    private static final Path outputPath = Paths.get("target","testout","integrationtesting","oracle","pk");

    private static Database database;

    @Container
    public static OracleContainer oracleContainer =
            new OracleContainer("gvenzl/oracle-xe:11-slim")
                    .usingSid()
                    .withInitScript("integrationTesting/oracle/dbScripts/pklogging.sql");

    @BeforeEach
    public synchronized void gatheringSchemaDetailsTest() throws SQLException, IOException, ScriptException, URISyntaxException {
        if (database == null) {
            createDatabaseRepresentation();
        }
    }

    private void createDatabaseRepresentation() throws SQLException, IOException {
        String[] args = {
                "-t", "orathin",
                "-db", oracleContainer.getSid(),
                "-s", "DBUSER",
                "-cat", "%",
                "-o", outputPath.toString(),
                "-u", "dbuser",
                "-p", "dbuser123",
                "-host", oracleContainer.getHost(),
                "-port", oracleContainer.getOraclePort().toString()
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
