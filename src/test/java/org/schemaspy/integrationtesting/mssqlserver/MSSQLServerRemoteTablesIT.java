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
package org.schemaspy.integrationtesting.mssqlserver;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.schemaspy.integrationtesting.MssqlSuite;
import org.schemaspy.model.Database;
import org.schemaspy.testing.SuiteContainerExtension;

import java.io.IOException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.schemaspy.testing.DatabaseFixture.database;

/**
 * @author Rafal Kasa
 * @author Nils Petzaell
 */
@DisabledOnOs(value = OS.MAC, architectures = {"aarch64"})
class MSSQLServerRemoteTablesIT {

    private static Database database;

    @RegisterExtension
    static SuiteContainerExtension container = MssqlSuite.SUITE_CONTAINER;

    @BeforeAll
    static void gatheringSchemaDetailsTest() throws SQLException, IOException {
        String[] args = {
                "-t", "mssql17",
                "-db", "ACME",
                "-o", "target/testout/integrationtesting/mssql/remote_table",
                "-u", "schemaspy",
                "-p", "qwerty123!",
                "-host", container.getHost(),
                "-port", container.getPort(1433)
        };
        database = database(args);
    }

    @Test
    void databaseShouldHaveZeroRemoteTables() {
        assertThat(database.getRemoteTables()).isEmpty();
    }
}
