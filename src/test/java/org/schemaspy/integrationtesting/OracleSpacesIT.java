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
package org.schemaspy.integrationtesting;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.schemaspy.Config;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.input.dbms.service.DatabaseService;
import org.schemaspy.input.dbms.service.SqlService;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.testing.AssumeClassIsPresentRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.OracleContainer;

import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * @author Nils Petzaell
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class OracleSpacesIT {
    @Autowired
    private SqlService sqlService;

    @Autowired
    private DatabaseService databaseService;

    @Mock
    private ProgressListener progressListener;

    @MockBean
    private CommandLineArguments arguments;

    @MockBean
    private CommandLineRunner commandLineRunner;

    private static Database database;

    public static JdbcContainerRule<OracleContainer> jdbcContainerRule =
            new JdbcContainerRule<>(() -> new OracleContainer("wnameless/oracle-xe-11g"))
                    .assumeDockerIsPresent()
                    .withAssumptions(assumeDriverIsPresent())
                    .withInitScript("integrationTesting/oracleSpacesIT/dbScripts/spaces_in_table_names.sql");

    public static TestRule jdbcDriverClassPresentRule = new AssumeClassIsPresentRule("oracle.jdbc.OracleDriver");

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

    private void createDatabaseRepresentation() throws SQLException, IOException, URISyntaxException {
        String[] args = {
                "-t", "orathin",
                "-db", jdbcContainerRule.getContainer().getSid(),
                "-s", "ORASPACEIT",
                "-cat", "%",
                "-o", "target/integrationtesting/ORASPACEIT",
                "-u", "oraspaceit",
                "-p", "oraspaceit123",
                "-host", jdbcContainerRule.getContainer().getContainerIpAddress(),
                "-port", jdbcContainerRule.getContainer().getOraclePort().toString()
        };
        given(arguments.getOutputDirectory()).willReturn(new File("target/integrationtesting/ORASPACEIT"));
        given(arguments.getDatabaseType()).willReturn("orathin");
        given(arguments.getUser()).willReturn("orait");
        given(arguments.getSchema()).willReturn("ORASPACEIT");
        given(arguments.getCatalog()).willReturn("%");
        given(arguments.getDatabaseName()).willReturn(jdbcContainerRule.getContainer().getSid());
        Config config = new Config(args);
        sqlService.connect(config);
        Database database = new Database(
                sqlService.getDbmsMeta(),
                arguments.getDatabaseName(),
                arguments.getCatalog(),
                arguments.getSchema()
        );
        databaseService.gatherSchemaDetails(config, database, null, progressListener);
        this.database = database;
    }

    @Test
    public void databaseShouldHaveTableWithSpaces() {
        assertThat(database.getTables()).extracting(t -> t.getName()).contains("test 1.0");
    }
}
