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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MSSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.script.ScriptException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.schemaspy.integrationtesting.MssqlServerSuite.IMAGE_NAME;

/**
 * @author Rafal Kasa
 * @author Nils Petzaell
 */
@DisabledOnOs(value = OS.MAC, architectures = {"aarch64"})
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Main.class)
@DirtiesContext
@Testcontainers(disabledWithoutDocker = true)
public class MSSQLServerRemoteTablesIT {

    private SqlService sqlService = new SqlService();

    @Mock
    private ProgressListener progressListener;

    private static Database database;

    @Container
    public static MSSQLContainer mssqlContainer =
            new MSSQLContainer(IMAGE_NAME)
                    .withInitScript("integrationTesting/mssqlserver/dbScripts/mssql_remote_tables.sql");

    @BeforeEach
    public synchronized void gatheringSchemaDetailsTest() throws SQLException, IOException, ScriptException, URISyntaxException {
        if (database == null) {
            createDatabaseRepresentation();
        }
    }

    private void createDatabaseRepresentation() throws SQLException, IOException {
        String[] args = {
                "-t", "mssql17",
                "-db", "ACME",
                "-o", "target/testout/integrationtesting/mssql/remote_table",
                "-u", "schemaspy",
                "-p", "qwerty123!",
                "-host", mssqlContainer.getHost(),
                "-port", mssqlContainer.getMappedPort(1433).toString()
        };
        CommandLineArguments arguments = new CommandLineArgumentParser(
            new CommandLineArguments(),
            (option) -> null
        ).parse(args);
        DatabaseMetaData databaseMetaData = sqlService.connect(arguments.getConnectionConfig());
        Database database = new Database(
                sqlService.getDbmsMeta(),
                arguments.getConnectionConfig().getDatabaseName(),
                databaseMetaData.getConnection().getCatalog(),
                databaseMetaData.getConnection().getSchema()
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(arguments.getProcessingConfig()).gatherSchemaDetails(database, null, progressListener);
        MSSQLServerRemoteTablesIT.database = database;
    }

    @Test
    void databaseShouldHaveZeroRemoteTables() {
        assertThat(database.getRemoteTables()).isEmpty();
    }
}
