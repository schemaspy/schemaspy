/*
 * Copyright (C) 2017, 2018 Rafal Kasa
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
package org.schemaspy.testcontainer;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.schemaspy.Config;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.schemaspy.service.DatabaseService;
import org.schemaspy.service.SqlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.MSSQLServerContainer;

import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * @author Rafal Kasa
 * @author Nils Petzaell
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class MSSQLServerIT {

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

    private static Database abcdbo;
    private static Database abca;
    private static Database abcb;
    private static Database abcc;

    private static Database defdbo;
    private static Database defd;
    private static Database defe;
    private static Database deff;

    @ClassRule
    public static JdbcContainerRule<MSSQLServerContainer> jdbcContainerRule =
            new JdbcContainerRule<>(() -> new MSSQLServerContainer())
                    .assumeDockerIsPresent()
                    .withAssumptions(assumeDriverIsPresent())
                    .withInitScript("integrationTesting/dbScripts/mssql_comments.sql");

    @Before
    public synchronized void gatheringSchemaDetailsTest() throws SQLException, IOException, ScriptException, URISyntaxException {
        if (abcdbo == null) {
            abcdbo = createDatabaseRepresentation("abc", "dbo");
        }
        if (abca == null) {
            abca = createDatabaseRepresentation("abc", "a");
        }
        if (abcb == null) {
            abcb = createDatabaseRepresentation("abc", "b");
        }
        if (abcc == null) {
            abcc = createDatabaseRepresentation("abc", "c");
        }

        if (defdbo == null) {
            defdbo = createDatabaseRepresentation("def", "dbo");
        }
        if (defd == null) {
            defd = createDatabaseRepresentation("def", "d");
        }
        if (defe == null) {
            defe = createDatabaseRepresentation("def", "e");
        }
        if (deff == null) {
            deff = createDatabaseRepresentation("def", "f");
        }
    }

    private Database createDatabaseRepresentation(String db, String schema) throws SQLException, IOException, URISyntaxException {
        String[] args = {
                "-t", "mssql08",
                "-db", db,
                "-s", schema,
                "-cat", "%",
                "-o", "target/integrationtesting/mssql",
                "-u", "sa",
                "-p", jdbcContainerRule.getContainer().getPassword(),
                "-host", jdbcContainerRule.getContainer().getContainerIpAddress(),
                "-port", jdbcContainerRule.getContainer().getMappedPort(1433).toString()
        };
        given(arguments.getOutputDirectory()).willReturn(new File("target/integrationtesting/mssql"));
        given(arguments.getDatabaseType()).willReturn("mssql08");
        given(arguments.getUser()).willReturn("sa");
        given(arguments.getSchema()).willReturn(schema);
        given(arguments.getCatalog()).willReturn("%");
        given(arguments.getDatabaseName()).willReturn(db);
        Config config = new Config(args);
        DatabaseMetaData databaseMetaData = sqlService.connect(config);
        Database database = new Database(
                databaseMetaData,
                arguments.getDatabaseName(),
                arguments.getCatalog(),
                arguments.getSchema()
        );
        databaseService.gatheringSchemaDetails(config, database, null, progressListener);
        return database;
    }

    @Test
    public void validateCatalogComments() {
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(abcdbo.getCatalog().getComment()).isEqualTo("ABC comment");
        softAssertions.assertThat(abca.getCatalog().getComment()).isEqualTo("ABC comment");
        softAssertions.assertThat(abcb.getCatalog().getComment()).isEqualTo("ABC comment");
        softAssertions.assertThat(abcc.getCatalog().getComment()).isEqualTo("ABC comment");

        softAssertions.assertThat(defdbo.getCatalog().getComment()).isEqualTo("DEF comment");
        softAssertions.assertThat(defd.getCatalog().getComment()).isEqualTo("DEF comment");
        softAssertions.assertThat(defe.getCatalog().getComment()).isEqualTo("DEF comment");
        softAssertions.assertThat(deff.getCatalog().getComment()).isEqualTo("DEF comment");
        softAssertions.assertAll();
    }

    @Test
    public void validateSchemaComments() {
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(abcdbo.getSchema().getComment()).isEqualTo("ABC Schema dbo comment");
        softAssertions.assertThat(abca.getSchema().getComment()).isEqualTo("ABC Schema A comment");
        softAssertions.assertThat(abcb.getSchema().getComment()).isEqualTo("ABC Schema B comment");
        softAssertions.assertThat(abcc.getSchema().getComment()).isEqualTo("ABC Schema C comment");

        softAssertions.assertThat(defdbo.getSchema().getComment()).isEqualTo("DEF Schema dbo comment");
        softAssertions.assertThat(defd.getSchema().getComment()).isEqualTo("DEF Schema D comment");
        softAssertions.assertThat(defe.getSchema().getComment()).isEqualTo("DEF Schema E comment");
        softAssertions.assertThat(deff.getSchema().getComment()).isEqualTo("DEF Schema F comment");
        softAssertions.assertAll();
    }

    @Test
    public void validateTableComments() {
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(abcdbo.getTablesMap().get("ATable").getComments()).isEqualTo("ABC Schema dbo ATable comment");
        softAssertions.assertThat(abca.getTablesMap().get("ATable").getComments()).isEqualTo("ABC Schema A ATable comment");
        softAssertions.assertThat(abcb.getTablesMap().get("ATable").getComments()).isEqualTo("ABC Schema B ATable comment");
        softAssertions.assertThat(abcc.getTablesMap().get("ATable").getComments()).isEqualTo("ABC Schema C ATable comment");

        softAssertions.assertThat(defdbo.getTablesMap().get("ATable").getComments()).isEqualTo("DEF Schema dbo ATable comment");
        softAssertions.assertThat(defd.getTablesMap().get("ATable").getComments()).isEqualTo("DEF Schema D ATable comment");
        softAssertions.assertThat(defe.getTablesMap().get("ATable").getComments()).isEqualTo("DEF Schema E ATable comment");
        softAssertions.assertThat(deff.getTablesMap().get("ATable").getComments()).isEqualTo("DEF Schema F ATable comment");
        softAssertions.assertAll();
    }

    @Test
    public void validateColumnComments() {
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(abcdbo.getTablesMap().get("ATable").getColumnsMap().get("Description").getComments()).isEqualTo("ABC Schema dbo ATable Column comment");
        softAssertions.assertThat(abca.getTablesMap().get("ATable").getColumnsMap().get("Description").getComments()).isEqualTo("ABC Schema A ATable Column comment");
        softAssertions.assertThat(abcb.getTablesMap().get("ATable").getColumnsMap().get("Description").getComments()).isEqualTo("ABC Schema B ATable Column comment");
        softAssertions.assertThat(abcc.getTablesMap().get("ATable").getColumnsMap().get("Description").getComments()).isEqualTo("ABC Schema C ATable Column comment");

        softAssertions.assertThat(defdbo.getTablesMap().get("ATable").getColumnsMap().get("Description").getComments()).isEqualTo("DEF Schema dbo ATable Column comment");
        softAssertions.assertThat(defd.getTablesMap().get("ATable").getColumnsMap().get("Description").getComments()).isEqualTo("DEF Schema D ATable Column comment");
        softAssertions.assertThat(defe.getTablesMap().get("ATable").getColumnsMap().get("Description").getComments()).isEqualTo("DEF Schema E ATable Column comment");
        softAssertions.assertThat(deff.getTablesMap().get("ATable").getColumnsMap().get("Description").getComments()).isEqualTo("DEF Schema F ATable Column comment");
        softAssertions.assertAll();
    }

}
