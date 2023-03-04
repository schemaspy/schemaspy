/*
 * Copyright (C) 2017, 2018 Nils Petzaell
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
import org.schemaspy.Config;
import org.schemaspy.cli.CommandLineArgumentParser;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.input.dbms.service.DatabaseServiceFactory;
import org.schemaspy.input.dbms.service.SqlService;
import org.schemaspy.integrationtesting.MysqlSuite;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.model.Table;
import org.schemaspy.testing.SuiteOrTestJdbcContainerRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.MySQLContainer;

import javax.script.ScriptException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Nils Petzaell
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class MysqlKeyWordTableIT {

    private static final Path outputPath = Paths.get("target","testout","integrationtesting","mysql","keywords");

    @Autowired
    private SqlService sqlService;

    @Mock
    private ProgressListener progressListener;

    @Autowired
    private CommandLineArgumentParser commandLineArgumentParser;

    private static Database database;

    @SuppressWarnings("unchecked")
    @ClassRule
    public static JdbcContainerRule<MySQLContainer<?>> jdbcContainerRule =
            new SuiteOrTestJdbcContainerRule<MySQLContainer<?>>(
                    MysqlSuite.jdbcContainerRule,
                    new JdbcContainerRule<MySQLContainer<?>>(() -> new MySQLContainer<>("mysql:5"))
                            .assumeDockerIsPresent()
                            .withAssumptions(assumeDriverIsPresent())
                            .withQueryString("?useSSL=false")
                            .withInitScript("integrationTesting/mysql/dbScripts/keywordtableit.sql")
                            .withInitUser("root", "test")
            );

    @Before
    public synchronized void createDatabaseRepresentation() throws SQLException, IOException, ScriptException, URISyntaxException {
        if (database == null) {
            doCreateDatabaseRepresentation();
        }
    }

    private void doCreateDatabaseRepresentation() throws SQLException, IOException {
        String[] args = {
                "-t", "mysql",
                "-db", "keywordtableit",
                "-s", "keywordtableit",
                "-u", "test",
                "-p", "test",
                "-cat", "%",
                "-host", jdbcContainerRule.getContainer().getContainerIpAddress(),
                "-port", jdbcContainerRule.getContainer().getMappedPort(3306).toString(),
                "-o", outputPath.toString(),
                "-connprops", "useSSL\\=false"
        };
        CommandLineArguments arguments = commandLineArgumentParser.parse(args);
        Config config = new Config(args);
        sqlService.connect(arguments, config);
        Database database = new Database(
                sqlService.getDbmsMeta(),
                arguments.getDatabaseName(),
                arguments.getCatalog(),
                arguments.getSchema()
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(config).gatherSchemaDetails(database, null, progressListener);
        MysqlKeyWordTableIT.database = database;
    }

    @Test
    public void hasATableNamedDistinct() {
        assertThat(database.getTables()).extracting(Table::getName).contains("DISTINCT");
    }
}
