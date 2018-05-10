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

    private static Database abc_dbo;
    private static Database abc_a;
    private static Database abc_b;
    private static Database abc_c;

    private static Database abc_test_dbo;
    private static Database abc_test_a;
    private static Database abc_test_b;
    private static Database abc_test_c;

    private static Database def_dbo;
    private static Database def_d;
    private static Database def_e;
    private static Database def_f;

    @ClassRule
    public static JdbcContainerRule<MSSQLServerContainer> jdbcContainerRule =
            new JdbcContainerRule<>(() -> new MSSQLServerContainer())
                    .assumeDockerIsPresent()
                    .withAssumptions(assumeDriverIsPresent())
                    .withInitScript("integrationTesting/dbScripts/mssql_comments.sql");

    @Before
    public synchronized void gatheringSchemaDetailsTest() throws SQLException, IOException, ScriptException, URISyntaxException {
        if (abc_dbo == null) {
            abc_dbo = createDatabaseRepresentation("abc", "dbo");
        }
        if (abc_a == null) {
            abc_a = createDatabaseRepresentation("abc", "a");
        }
        if (abc_b == null) {
            abc_b = createDatabaseRepresentation("abc", "b");
        }
        if (abc_c == null) {
            abc_c = createDatabaseRepresentation("abc", "c");
        }

        if (abc_test_dbo == null) {
            abc_test_dbo = createDatabaseRepresentation("abc_test", "dbo");
        }
        if (abc_test_a == null) {
            abc_test_a = createDatabaseRepresentation("abc_test", "a");
        }
        if (abc_test_b == null) {
            abc_test_b = createDatabaseRepresentation("abc_test", "b");
        }
        if (abc_test_c == null) {
            abc_test_c = createDatabaseRepresentation("abc_test", "c");
        }

        if (def_dbo == null) {
            def_dbo = createDatabaseRepresentation("def", "dbo");
        }
        if (def_d == null) {
            def_d = createDatabaseRepresentation("def", "d");
        }
        if (def_e == null) {
            def_e = createDatabaseRepresentation("def", "e");
        }
        if (def_f == null) {
            def_f = createDatabaseRepresentation("def", "f");
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
        softAssertions.assertThat(abc_dbo.getCatalog().getComment()).isEqualTo("ABC comment");
        softAssertions.assertThat(abc_a.getCatalog().getComment()).isEqualTo("ABC comment");
        softAssertions.assertThat(abc_b.getCatalog().getComment()).isEqualTo("ABC comment");
        softAssertions.assertThat(abc_c.getCatalog().getComment()).isEqualTo("ABC comment");

        softAssertions.assertThat(abc_test_dbo.getCatalog().getComment()).isEqualTo("ABC_TEST comment");
        softAssertions.assertThat(abc_test_a.getCatalog().getComment()).isEqualTo("ABC_TEST comment");
        softAssertions.assertThat(abc_test_b.getCatalog().getComment()).isEqualTo("ABC_TEST comment");
        softAssertions.assertThat(abc_test_c.getCatalog().getComment()).isEqualTo("ABC_TEST comment");

        softAssertions.assertThat(def_dbo.getCatalog().getComment()).isEqualTo("DEF comment");
        softAssertions.assertThat(def_d.getCatalog().getComment()).isEqualTo("DEF comment");
        softAssertions.assertThat(def_e.getCatalog().getComment()).isEqualTo("DEF comment");
        softAssertions.assertThat(def_f.getCatalog().getComment()).isEqualTo("DEF comment");
        softAssertions.assertAll();
    }

    @Test
    public void validateSchemaComments() {
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(abc_dbo.getSchema().getComment()).isEqualTo("ABC Schema dbo comment");
        softAssertions.assertThat(abc_a.getSchema().getComment()).isEqualTo("ABC Schema A comment");
        softAssertions.assertThat(abc_b.getSchema().getComment()).isEqualTo("ABC Schema B comment");
        softAssertions.assertThat(abc_c.getSchema().getComment()).isEqualTo("ABC Schema C comment");

        softAssertions.assertThat(abc_test_dbo.getSchema().getComment()).isEqualTo("ABC_TEST Schema dbo comment");
        softAssertions.assertThat(abc_test_a.getSchema().getComment()).isEqualTo("ABC_TEST Schema A comment");
        softAssertions.assertThat(abc_test_b.getSchema().getComment()).isEqualTo("ABC_TEST Schema B comment");
        softAssertions.assertThat(abc_test_c.getSchema().getComment()).isEqualTo("ABC_TEST Schema C comment");

        softAssertions.assertThat(def_dbo.getSchema().getComment()).isEqualTo("DEF Schema dbo comment");
        softAssertions.assertThat(def_d.getSchema().getComment()).isEqualTo("DEF Schema D comment");
        softAssertions.assertThat(def_e.getSchema().getComment()).isEqualTo("DEF Schema E comment");
        softAssertions.assertThat(def_f.getSchema().getComment()).isEqualTo("DEF Schema F comment");
        softAssertions.assertAll();
    }

    @Test
    public void validateTableComments() {
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(abc_dbo.getTablesMap().get("ATable").getComments()).isEqualTo("ABC Schema dbo ATable comment");
        softAssertions.assertThat(abc_a.getTablesMap().get("ATable").getComments()).isEqualTo("ABC Schema A ATable comment");
        softAssertions.assertThat(abc_b.getTablesMap().get("ATable").getComments()).isEqualTo("ABC Schema B ATable comment");
        softAssertions.assertThat(abc_c.getTablesMap().get("ATable").getComments()).isEqualTo("ABC Schema C ATable comment");

        softAssertions.assertThat(abc_test_dbo.getTablesMap().get("ATable").getComments()).isEqualTo("ABC_TEST Schema dbo ATable comment");
        softAssertions.assertThat(abc_test_a.getTablesMap().get("ATable").getComments()).isEqualTo("ABC_TEST Schema A ATable comment");
        softAssertions.assertThat(abc_test_b.getTablesMap().get("ATable").getComments()).isEqualTo("ABC_TEST Schema B ATable comment");
        softAssertions.assertThat(abc_test_c.getTablesMap().get("ATable").getComments()).isEqualTo("ABC_TEST Schema C ATable comment");

        softAssertions.assertThat(def_dbo.getTablesMap().get("ATable").getComments()).isEqualTo("DEF Schema dbo ATable comment");
        softAssertions.assertThat(def_d.getTablesMap().get("ATable").getComments()).isEqualTo("DEF Schema D ATable comment");
        softAssertions.assertThat(def_e.getTablesMap().get("ATable").getComments()).isEqualTo("DEF Schema E ATable comment");
        softAssertions.assertThat(def_f.getTablesMap().get("ATable").getComments()).isEqualTo("DEF Schema F ATable comment");
        softAssertions.assertAll();
    }

    @Test
    public void validateColumnComments() {
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(abc_dbo.getTablesMap().get("ATable").getColumnsMap().get("Description").getComments()).isEqualTo("ABC Schema dbo ATable Column comment");
        softAssertions.assertThat(abc_a.getTablesMap().get("ATable").getColumnsMap().get("Description").getComments()).isEqualTo("ABC Schema A ATable Column comment");
        softAssertions.assertThat(abc_b.getTablesMap().get("ATable").getColumnsMap().get("Description").getComments()).isEqualTo("ABC Schema B ATable Column comment");
        softAssertions.assertThat(abc_c.getTablesMap().get("ATable").getColumnsMap().get("Description").getComments()).isEqualTo("ABC Schema C ATable Column comment");

        softAssertions.assertThat(abc_test_dbo.getTablesMap().get("ATable").getColumnsMap().get("Description").getComments()).isEqualTo("ABC_TEST Schema dbo ATable Column comment");
        softAssertions.assertThat(abc_test_a.getTablesMap().get("ATable").getColumnsMap().get("Description").getComments()).isEqualTo("ABC_TEST Schema A ATable Column comment");
        softAssertions.assertThat(abc_test_b.getTablesMap().get("ATable").getColumnsMap().get("Description").getComments()).isEqualTo("ABC_TEST Schema B ATable Column comment");
        softAssertions.assertThat(abc_test_c.getTablesMap().get("ATable").getColumnsMap().get("Description").getComments()).isEqualTo("ABC_TEST Schema C ATable Column comment");

        softAssertions.assertThat(def_dbo.getTablesMap().get("ATable").getColumnsMap().get("Description").getComments()).isEqualTo("DEF Schema dbo ATable Column comment");
        softAssertions.assertThat(def_d.getTablesMap().get("ATable").getColumnsMap().get("Description").getComments()).isEqualTo("DEF Schema D ATable Column comment");
        softAssertions.assertThat(def_e.getTablesMap().get("ATable").getColumnsMap().get("Description").getComments()).isEqualTo("DEF Schema E ATable Column comment");
        softAssertions.assertThat(def_f.getTablesMap().get("ATable").getColumnsMap().get("Description").getComments()).isEqualTo("DEF Schema F ATable Column comment");
        softAssertions.assertAll();
    }

}
