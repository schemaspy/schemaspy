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
import org.schemaspy.model.Table;
import org.schemaspy.testing.SQLScriptsRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.InformixContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Nils Petzaell
 */
@DisabledOnOs(value = OS.MAC, architectures = {"aarch64"})
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Main.class)
@DirtiesContext
@Testcontainers(disabledWithoutDocker = true)
public class InformixRoutinesIT {

    private SqlService sqlService = new SqlService();

    @Mock
    private ProgressListener progressListener;

    private static Database database;

    @Container
    public static InformixContainer informixContainer =
            new InformixContainer();

    @BeforeAll
    public static void initFunction() throws SQLException {
        new SQLScriptsRunner(
                "integrationTesting/informix/dbScripts/informixroutines.sql",
                "\n\n\n"
        ).accept(informixContainer.createConnection(""));
    }

    @BeforeEach
    public synchronized void gatheringSchemaDetailsTest() throws SQLException, IOException {
        if (database == null) {
            createDatabaseRepresentation();
        }
    }

    private void createDatabaseRepresentation() throws SQLException, IOException {
        String[] args = {
                "-t", "informix",
                "-db", "test",
                "-s", "informix",
                "-cat", "test",
                "-server", "dev",
                "-o", "target/testout/integrationtesting/informix/routines",
                "-u", informixContainer.getUsername(),
                "-p", informixContainer.getPassword(),
                "-host", informixContainer.getHost(),
                "-port", informixContainer.getJdbcPort().toString()
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
        InformixRoutinesIT.database = database;
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
