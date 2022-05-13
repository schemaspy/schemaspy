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
package org.schemaspy.integrationtesting.pgsql;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.schemaspy.Config;
import org.schemaspy.cli.CommandLineArgumentParser;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.input.dbms.service.DatabaseServiceFactory;
import org.schemaspy.input.dbms.service.SqlService;
import org.schemaspy.integrationtesting.PgSqlSuite;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.model.Routine;
import org.schemaspy.testing.SQLScriptsRunner;
import org.schemaspy.testing.SuiteOrTestJdbcContainerRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class PgSqlPloyFuncIT {

    private static final Path outputPath = Paths.get("target","testout","integrationtesting","pgsql","polyfunc");

    @Autowired
    private SqlService sqlService;

    @Mock
    private ProgressListener progressListener;

    @Autowired
    private CommandLineArgumentParser commandLineArgumentParser;

    private static Database database;

    @SuppressWarnings("unchecked")
    @ClassRule
    public static JdbcContainerRule<PostgreSQLContainer<?>> jdbcContainerRule =
            new SuiteOrTestJdbcContainerRule<PostgreSQLContainer<?>>(
                    PgSqlSuite.jdbcContainerRule,
                    new JdbcContainerRule<PostgreSQLContainer<?>>(() -> new PostgreSQLContainer<>("postgres:10.4"))
                            .assumeDockerIsPresent()
                            .withAssumptions(assumeDriverIsPresent())
                            .withInitFunctions(new SQLScriptsRunner("integrationTesting/pgsql/dbScripts/polyfunc.sql", "\n\n\n"))
            );

    @Before
    public synchronized void createDatabaseRepresentation() throws SQLException, IOException {
        if (database == null) {
            doCreateDatabaseRepresentation();
        }
    }

    private void doCreateDatabaseRepresentation() throws SQLException, IOException {
        String[] args = {
                "-t", "pgsql",
                "-db", "test",
                "-s", "polyfunc",
                "-cat", "%",
                "-o", outputPath.toString(),
                "-u", "test",
                "-p", "test",
                "-host", jdbcContainerRule.getContainer().getContainerIpAddress(),
                "-port", jdbcContainerRule.getContainer().getMappedPort(5432).toString()
        };
        CommandLineArguments arguments = commandLineArgumentParser.parse(args);
        Config config = new Config(args);
        sqlService.connect(config);
        Database database = new Database(
                sqlService.getDbmsMeta(),
                arguments.getDatabaseName(),
                arguments.getCatalog(),
                arguments.getSchema()
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(config).gatherSchemaDetails(database, null, progressListener);
        PgSqlPloyFuncIT.database = database;
    }

    @Test
    public void databaseShouldExist() {
        assertThat(database).isNotNull();
        assertThat(database.getName()).isEqualToIgnoringCase("test");
    }

    @Test
    public void hasThreeFunctions() {
        assertThat(database.getRoutines().size()).isEqualTo(3);
    }

    @Test
    public void validateAllThreeFunctionNames() {
        assertThat(database.getRoutines()).extracting(Routine::getName).containsExactlyInAnyOrder("bar(bigint)", "foo(text)", "foo(bigint, text)");
    }
}
