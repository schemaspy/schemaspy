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
package org.schemaspy.integrationtesting.mariadb;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.schemaspy.model.Database;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.schemaspy.testing.DatabaseFixture.database;

/**
 * @author Nils Petzaell
 */
@Disabled
@Testcontainers(disabledWithoutDocker = true)
class MariaDbRoutinesIT {

    private static Database database;

    @Container
    static MariaDBContainer container =
        new MariaDBContainer<>("mariadb:10.2")
            .withInitScript("integrationTesting/mariadb/dbScripts/routinesit.sql");

    @BeforeAll
    static void createDatabaseRepresentation() throws SQLException, IOException {
        String[] args = {
            "-t", "mariadb",
            "-db", "test",
            "-s", "test",
            "-cat", "%",
            "-o", "target/testout/integrationtesting/mariadb/routines",
            "-u", container.getUsername(),
            "-p", container.getPassword(),
            "-host", container.getHost(),
            "-port", container.getMappedPort(3306).toString()
        };
        database = database(args);
    }

    @Test
    void databaseShouldExist() {
        assertThat(database).isNotNull();
        assertThat(database.getName()).isEqualToIgnoringCase("test");
    }

    @Test
    void databaseShouldHaveRoutines() {
        assertThat(database.getRoutinesMap().get("no_det").isDeterministic()).isFalse();
        assertThat(database.getRoutinesMap().get("yes_det").isDeterministic()).isTrue();
    }
}
