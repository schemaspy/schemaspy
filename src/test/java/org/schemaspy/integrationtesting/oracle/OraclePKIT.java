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
package org.schemaspy.integrationtesting.oracle;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.schemaspy.Config;
import org.schemaspy.cli.CommandLineArgumentParser;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.input.dbms.service.DatabaseServiceFactory;
import org.schemaspy.input.dbms.service.SqlService;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableIndex;
import org.schemaspy.testing.AssumeClassIsPresentRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.OracleContainer;

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
public class OraclePKIT {

    private static final Path outputPath = Paths.get("target","testout","integrationtesting","oracle","pk");

    @Autowired
    private SqlService sqlService;

    @Mock
    private ProgressListener progressListener;

    private static Database database;

    public static TestRule jdbcDriverClassPresentRule = new AssumeClassIsPresentRule("oracle.jdbc.OracleDriver");

    @SuppressWarnings("unchecked")
    public static JdbcContainerRule<OracleContainer> jdbcContainerRule =
            new JdbcContainerRule<>(() -> new OracleContainer("gvenzl/oracle-xe:11-slim").usingSid())
            .assumeDockerIsPresent()
            .withAssumptions(assumeDriverIsPresent())
            .withInitScript("integrationTesting/oracle/dbScripts/pklogging.sql");

    @ClassRule
    public static final TestRule chain = RuleChain
            .outerRule(jdbcContainerRule)
            .around(jdbcDriverClassPresentRule);

    @Before
    public synchronized void gatheringSchemaDetailsTest() throws SQLException, IOException, ScriptException, URISyntaxException {
        if (database == null) {
            createDatabaseRepresentation();
        }
    }

    private void createDatabaseRepresentation() throws SQLException, IOException {
        String[] args = {
                "-t", "orathin",
                "-db", jdbcContainerRule.getContainer().getSid(),
                "-s", "DBUSER",
                "-cat", "%",
                "-o", outputPath.toString(),
                "-u", "dbuser",
                "-p", "dbuser123",
                "-host", jdbcContainerRule.getContainer().getContainerIpAddress(),
                "-port", jdbcContainerRule.getContainer().getOraclePort().toString()
        };
        CommandLineArguments arguments = new CommandLineArgumentParser(
            new CommandLineArguments(),
            (option) -> null
        ).parse(args);
        Config config = new Config(args);
        sqlService.connect(arguments, config);
        Database database = new Database(
                sqlService.getDbmsMeta(),
                arguments.getDatabaseName(),
                arguments.getCatalog(),
                arguments.getSchema()
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(config).gatherSchemaDetails(database, null, progressListener);
        OraclePKIT.database = database;
    }

    @Test
    public void databaseShouldHaveTable() {
        Table table = getTable("GIWSDURATIONHISTORY");
        assertThat(table).isNotNull();
    }

    @Test
    public void tableShouldHavePKIndex() {
        Table table = getTable("GIWSDURATIONHISTORY");
        assertThat(table.getIndexes().stream().filter(TableIndex::isPrimaryKey).count()).isEqualTo(1);
    }

    private Table getTable(String tableName) {
        return database.getTablesMap().get(tableName);
    }
}
