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

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.schemaspy.integrationtesting.MssqlSuite;
import org.schemaspy.model.Database;
import org.schemaspy.testing.SuiteContainerExtension;

import java.io.IOException;
import java.sql.SQLException;

import static org.schemaspy.testing.DatabaseFixture.database;

/**
 * @author Rafal Kasa
 * @author Nils Petzaell
 */
@DisabledOnOs(value = OS.MAC, architectures = {"aarch64"})
class MSSQLServerCommentsIT {

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

    @RegisterExtension
    static SuiteContainerExtension container = MssqlSuite.SUITE_CONTAINER;

    @BeforeAll
    static void gatheringSchemaDetailsTest() throws SQLException, IOException {
        abc_dbo = createDatabaseRepresentation("ABC", "dbo");
        abc_a = createDatabaseRepresentation("ABC", "A");
        abc_b = createDatabaseRepresentation("ABC", "B");
        abc_c = createDatabaseRepresentation("ABC", "C");
        abc_test_dbo = createDatabaseRepresentation("ABC_TEST", "dbo");
        abc_test_a = createDatabaseRepresentation("ABC_TEST", "A");
        abc_test_b = createDatabaseRepresentation("ABC_TEST", "B");
        abc_test_c = createDatabaseRepresentation("ABC_TEST", "C");
        def_dbo = createDatabaseRepresentation("DEF", "dbo");
        def_d = createDatabaseRepresentation("DEF", "D");
        def_e = createDatabaseRepresentation("DEF", "E");
        def_f = createDatabaseRepresentation("DEF", "F");
    }

    private static Database createDatabaseRepresentation(String db, String schema) throws SQLException, IOException {
        String[] args = {
                "-t", "mssql17",
                "-db", db,
                "-s", schema,
                "-cat", "%",
                "-o", "target/testout/integrationtesting/mssql/comments",
                "-u", "sa",
                "-p", container.getPassword(),
                "-host", container.getHost(),
                "-port", container.getPort(1433)
        };
        return database(args);
    }

    @Test
    void validateCatalogComments() {
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
    void validateSchemaComments() {
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
    void validateTableComments() {
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
    void validateColumnComments() {
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
