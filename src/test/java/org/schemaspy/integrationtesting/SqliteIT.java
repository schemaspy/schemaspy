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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.schemaspy.Main;
import org.schemaspy.cli.CommandLineArgumentParser;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.input.dbms.service.DatabaseServiceFactory;
import org.schemaspy.input.dbms.service.SqlService;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Nils Petzaell
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class)
@DirtiesContext
public class SqliteIT {

    private SqlService sqlService = new SqlService();

    @Mock
    private ProgressListener progressListener;

    private static Database database;

    @Before
    public synchronized void createDatabaseRepresentation() throws SQLException, IOException {
        if (database == null) {
            doCreateDatabaseRepresentation();
        }
    }

    private void doCreateDatabaseRepresentation() throws SQLException, IOException {
        String[] args = {
                "-t", "sqlite-xerial",
                "-db", "src/test/resources/integrationTesting/sqlite/database/chinook.db",
                "-s", "chinook",
                "-cat", "chinook",
                "-o", "target/integrationtesting/sqlite",
                "-sso"
        };
        CommandLineArguments arguments = new CommandLineArgumentParser(
            new CommandLineArguments(),
            (option) -> null
        ).parse(args);
        sqlService.connect(arguments.getConnectionConfig());
        Database database = new Database(
                sqlService.getDbmsMeta(),
                arguments.getConnectionConfig().getDatabaseName(),
                arguments.getCatalog(),
                arguments.getSchema()
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(arguments.getProcessingConfig()).gatherSchemaDetails(database, null, progressListener);
        this.database = database;
    }

    @Test
    public void databaseContainsTable() {
        assertThat(database.getTables()).hasSize(11);
    }

    @Test
    public void databaseTablePlaylistsContainsPrimaryKey() {
        assertThat(database.getTablesMap().get("playlists").getPrimaryColumns()).isNotEmpty();
    }
}
