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
package org.schemaspy.integrationtesting.mysql;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.schemaspy.Main;
import org.schemaspy.cli.CommandLineArgumentParser;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.input.dbms.service.DatabaseServiceFactory;
import org.schemaspy.input.dbms.service.SqlService;
import org.schemaspy.integrationtesting.MysqlSuite;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.testing.SuiteOrTestJdbcContainerRule;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.MySQLContainer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Nils Petzaell
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class)
@DirtiesContext
public class MysqlRoutinesIT {

    private static final Path outputPath = Paths.get("target","testout","integrationtesting","mysql","routines");

    private SqlService sqlService = new SqlService();

    @Mock
    private ProgressListener progressListener;

    private static Database database;

    @SuppressWarnings("unchecked")
    @ClassRule
    public static JdbcContainerRule<MySQLContainer<?>> jdbcContainerRule =
            new SuiteOrTestJdbcContainerRule<MySQLContainer<?>>(
                    MysqlSuite.jdbcContainerRule,
                    new JdbcContainerRule<MySQLContainer<?>>(() -> new MySQLContainer<>("mysql:8-oracle"))
                            .assumeDockerIsPresent()
                            .withAssumptions(assumeDriverIsPresent())
                            .withQueryString("?useSSL=false&allowPublicKeyRetrieval=true")
                            .withInitScript("integrationTesting/mysql/dbScripts/routinesit.sql")
                            .withInitUser("root", "test")
            );

    @Before
    public synchronized void createDatabaseRepresentation() throws SQLException, IOException {
        if (database == null) {
            doCreateDatabaseRepresentation();
        }
    }

    private void doCreateDatabaseRepresentation() throws SQLException, IOException {
        String[] args = {
                "-t", "mysql",
                "-db", "routinesit",
                "-s", "routinesit",
                "-cat", "%",
                "-u", "test",
                "-p", "test",
                "-host", jdbcContainerRule.getContainer().getHost(),
                "-port", jdbcContainerRule.getContainer().getMappedPort(3306).toString(),
                "-o", outputPath.toString(),
                "-connprops", "useSSL\\=false;allowPublicKeyRetrieval\\=true"
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
        MysqlRoutinesIT.database = database;
    }

    @Test
    public void databaseShouldExist() {
        assertThat(database).isNotNull();
        assertThat(database.getName()).isEqualToIgnoringCase("routinesit");
    }

    @Test
    public void databaseShouldHaveRoutines() {
        assertThat(database.getRoutinesMap().get("no_det").isDeterministic()).isFalse();
        assertThat(database.getRoutinesMap().get("yes_det").isDeterministic()).isTrue();
    }
}
