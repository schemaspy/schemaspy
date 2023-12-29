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
import org.schemaspy.testing.testcontainers.SuiteContainerExtension;

import java.io.IOException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.schemaspy.testing.DatabaseFixture.database;

/**
 * @author Nils Petzaell
 */
@DisabledOnOs(value = OS.MAC, architectures = {"aarch64"})
class InformixRoutinesIT {

    private static Database database;

    @RegisterExtension
    static SuiteContainerExtension container = InformixSuite.SUITE_CONTAINER;

    @BeforeAll
    static void gatheringSchemaDetailsTest() throws SQLException, IOException {
        String[] args = {
                "-t", "informix",
                "-db", "testroutine",
                "-s", "informix",
                "-cat", "testroutine",
                "-server", "dev",
                "-o", "target/testout/integrationtesting/informix/routines",
                "-u", container.getUsername(),
                "-p", container.getPassword(),
                "-host", container.getHost(),
                "-port", container.getPort(9088),
                "--include-routine-definition"
        };
        database = database(args);
    }

    @Test
    void databaseShouldBePopulatedWithTableTest() {
        Table table = getTable("test");
        assertThat(table).isNotNull();
    }

    @Test
    void databaseShouldHaveCompleteRoutineDefinition() {
        String expecting = "CREATE FUNCTION gc_comb(partial1 LVARCHAR, partial2 LVARCHAR) RETURNING LVARCHAR; IF partial1 IS NULL OR partial1 = '' THEN RETURN partial2; ELIF partial2 IS NULL OR partial2 = '' THEN RETURN partial1; ELSE RETURN partial1 || ',' || partial2; END IF; END FUNCTION;";
        String actual = database.getRoutinesMap().get("gc_comb(lvarchar,lvarchar)").getDefinition().replaceAll("(\r\n|\r|\n)", " ").replaceAll("\\s\\s+", " ");
        assertThat(actual).isEqualToIgnoringCase(expecting);
    }

    private Table getTable(String tableName) {
        return database.getTablesMap().get(tableName);
    }
}
