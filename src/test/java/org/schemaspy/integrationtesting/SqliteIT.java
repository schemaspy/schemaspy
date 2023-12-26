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
package org.schemaspy.integrationtesting;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schemaspy.model.Database;

import static org.assertj.core.api.Assertions.assertThat;
import static org.schemaspy.testing.DatabaseFixture.database;

/**
 * @author Nils Petzaell
 */
class SqliteIT {

    private static Database database;

    @BeforeAll
    static void createDatabaseRepresentation() throws SQLException, IOException {
        String[] args = {
            "-t", "sqlite-xerial",
            "-db", "src/test/resources/integrationTesting/sqlite/database/chinook.db",
            "-s", "chinook",
            "-cat", "chinook",
            "-o", "target/integrationtesting/sqlite",
            "-sso"
        };
        database = database(args);
    }

    @Test
    void databaseContainsTable() {
        assertThat(database.getTables()).hasSize(11);
    }

    @Test
    void databaseTablePlaylistsContainsPrimaryKey() {
        assertThat(database.getTablesMap().get("playlists").getPrimaryColumns()).isNotEmpty();
    }
}
