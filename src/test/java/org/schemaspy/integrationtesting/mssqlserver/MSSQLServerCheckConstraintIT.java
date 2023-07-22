/*
 * Copyright (C) 2018 Nils Petzaell
 *
 * This file is part of SchemaSpy.
 *
 *  SchemaSpy is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  SchemaSpy is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with SchemaSpy.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.schemaspy.integrationtesting.mssqlserver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.schemaspy.cli.CommandLineArgumentParser;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.input.dbms.service.DatabaseServiceFactory;
import org.schemaspy.input.dbms.service.SqlService;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.model.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MSSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.script.ScriptException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.schemaspy.integrationtesting.MssqlServerSuite.IMAGE_NAME;

@DisabledOnOs(value = OS.MAC, architectures = {"aarch64"})
@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext
@Testcontainers(disabledWithoutDocker = true)
public class MSSQLServerCheckConstraintIT {
    @Autowired
    private SqlService sqlService;

    @Mock
    private ProgressListener progressListener;

    private static Database database;

    @Container
    public static MSSQLContainer mssqlContainer =
            new MSSQLContainer(IMAGE_NAME)
                    .withInitScript("integrationTesting/mssqlserver/dbScripts/check_constraint.sql");

    @BeforeEach
    public synchronized void gatheringSchemaDetailsTest() throws SQLException, IOException, ScriptException, URISyntaxException {
        if (database == null) {
            createDatabaseRepresentation();
        }
    }

    private void createDatabaseRepresentation() throws SQLException, IOException {
        String[] args = {
                "-t", "mssql17",
                "-db", "CheckConstraint",
                "-s", "CheckConstraint",
                "-cat", "CheckConstraint",
                "-o", "target/testout/integrationtesting/mssql/CheckConstraint",
                "-u", "sa",
                "-p", mssqlContainer.getPassword(),
                "-host", mssqlContainer.getContainerIpAddress(),
                "-port", mssqlContainer.getMappedPort(1433).toString()
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
        MSSQLServerCheckConstraintIT.database = database;
    }

    @Test
    void databaseShouldBePopulatedWithTableTest() {
        Table range = database.getTablesMap().get("range");
        assertThat(range.getCheckConstraints()).hasSize(1);
    }
}
