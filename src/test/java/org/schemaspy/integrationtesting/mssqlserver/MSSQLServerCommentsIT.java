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

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.mockito.BDDMockito.given;
import static org.schemaspy.integrationtesting.MssqlServerSuite.IMAGE_NAME;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

import javax.script.ScriptException;

import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.schemaspy.Config;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.integrationtesting.MssqlServerSuite;
import org.schemaspy.integrationtesting.TestServiceFixture;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.testing.SuiteOrTestJdbcContainerRule;
import org.testcontainers.containers.MSSQLContainer;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;

/**
 * @author Rafal Kasa
 * @author Nils Petzaell
 */
public class MSSQLServerCommentsIT {

	private final TestServiceFixture serviceFixture = new TestServiceFixture();

	@Mock
	private ProgressListener progressListener;

	@Mock
	private CommandLineArguments arguments;

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

	@SuppressWarnings("unchecked")
	@ClassRule
	public static JdbcContainerRule<MSSQLContainer> jdbcContainerRule = new SuiteOrTestJdbcContainerRule<MSSQLContainer>(
			MssqlServerSuite.jdbcContainerRule,
			new JdbcContainerRule<>(() -> new MSSQLContainer(IMAGE_NAME)).assumeDockerIsPresent()
					.withAssumptions(assumeDriverIsPresent())
					.withInitScript("integrationTesting/mssqlserver/dbScripts/mssql_comments.sql"));

	@Before
	public synchronized void gatheringSchemaDetailsTest()
			throws SQLException, IOException, ScriptException, URISyntaxException {
		MockitoAnnotations.openMocks(this);
		if (abc_dbo == null) {
			abc_dbo = createDatabaseRepresentation("ABC", "dbo");
		}
		if (abc_a == null) {
			abc_a = createDatabaseRepresentation("ABC", "A");
		}
		if (abc_b == null) {
			abc_b = createDatabaseRepresentation("ABC", "B");
		}
		if (abc_c == null) {
			abc_c = createDatabaseRepresentation("ABC", "C");
		}

		if (abc_test_dbo == null) {
			abc_test_dbo = createDatabaseRepresentation("ABC_TEST", "dbo");
		}
		if (abc_test_a == null) {
			abc_test_a = createDatabaseRepresentation("ABC_TEST", "A");
		}
		if (abc_test_b == null) {
			abc_test_b = createDatabaseRepresentation("ABC_TEST", "B");
		}
		if (abc_test_c == null) {
			abc_test_c = createDatabaseRepresentation("ABC_TEST", "C");
		}

		if (def_dbo == null) {
			def_dbo = createDatabaseRepresentation("DEF", "dbo");
		}
		if (def_d == null) {
			def_d = createDatabaseRepresentation("DEF", "D");
		}
		if (def_e == null) {
			def_e = createDatabaseRepresentation("DEF", "E");
		}
		if (def_f == null) {
			def_f = createDatabaseRepresentation("DEF", "F");
		}
	}

	private Database createDatabaseRepresentation(String db, String schema) throws SQLException, IOException {
		String[] args = { "-t", "mssql17", "-db", db, "-s", schema, "-cat", "%", "-o",
				"target/testout/integrationtesting/mssql/comments", "-u", "sa", "-p",
				jdbcContainerRule.getContainer().getPassword(), "-host",
				jdbcContainerRule.getContainer().getContainerIpAddress(), "-port",
				jdbcContainerRule.getContainer().getMappedPort(1433).toString() };
		given(arguments.getOutputDirectory()).willReturn(new File("target/testout/integrationtesting/mssql/comments"));
		given(arguments.getDatabaseType()).willReturn("mssql08");
		given(arguments.getUser()).willReturn("sa");
		given(arguments.getSchema()).willReturn(schema);
		given(arguments.getCatalog()).willReturn("%");
		given(arguments.getDatabaseName()).willReturn(db);
		Config config = new Config(args);
		serviceFixture.sqlService().connect(config);
		Database database = new Database(serviceFixture.sqlService().getDbmsMeta(), arguments.getDatabaseName(),
				arguments.getCatalog(), arguments.getSchema());
		serviceFixture.databaseService().gatherSchemaDetails(config, database, null, progressListener);
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
		softAssertions.assertThat(abc_dbo.getTablesMap().get("ATable").getComments())
				.isEqualTo("ABC Schema dbo ATable comment");
		softAssertions.assertThat(abc_a.getTablesMap().get("ATable").getComments())
				.isEqualTo("ABC Schema A ATable comment");
		softAssertions.assertThat(abc_b.getTablesMap().get("ATable").getComments())
				.isEqualTo("ABC Schema B ATable comment");
		softAssertions.assertThat(abc_c.getTablesMap().get("ATable").getComments())
				.isEqualTo("ABC Schema C ATable comment");

		softAssertions.assertThat(abc_test_dbo.getTablesMap().get("ATable").getComments())
				.isEqualTo("ABC_TEST Schema dbo ATable comment");
		softAssertions.assertThat(abc_test_a.getTablesMap().get("ATable").getComments())
				.isEqualTo("ABC_TEST Schema A ATable comment");
		softAssertions.assertThat(abc_test_b.getTablesMap().get("ATable").getComments())
				.isEqualTo("ABC_TEST Schema B ATable comment");
		softAssertions.assertThat(abc_test_c.getTablesMap().get("ATable").getComments())
				.isEqualTo("ABC_TEST Schema C ATable comment");

		softAssertions.assertThat(def_dbo.getTablesMap().get("ATable").getComments())
				.isEqualTo("DEF Schema dbo ATable comment");
		softAssertions.assertThat(def_d.getTablesMap().get("ATable").getComments())
				.isEqualTo("DEF Schema D ATable comment");
		softAssertions.assertThat(def_e.getTablesMap().get("ATable").getComments())
				.isEqualTo("DEF Schema E ATable comment");
		softAssertions.assertThat(def_f.getTablesMap().get("ATable").getComments())
				.isEqualTo("DEF Schema F ATable comment");
		softAssertions.assertAll();
	}

	@Test
	public void validateColumnComments() {
		SoftAssertions softAssertions = new SoftAssertions();
		softAssertions.assertThat(abc_dbo.getTablesMap().get("ATable").getColumnsMap().get("Description").getComments())
				.isEqualTo("ABC Schema dbo ATable Column comment");
		softAssertions.assertThat(abc_a.getTablesMap().get("ATable").getColumnsMap().get("Description").getComments())
				.isEqualTo("ABC Schema A ATable Column comment");
		softAssertions.assertThat(abc_b.getTablesMap().get("ATable").getColumnsMap().get("Description").getComments())
				.isEqualTo("ABC Schema B ATable Column comment");
		softAssertions.assertThat(abc_c.getTablesMap().get("ATable").getColumnsMap().get("Description").getComments())
				.isEqualTo("ABC Schema C ATable Column comment");

		softAssertions
				.assertThat(abc_test_dbo.getTablesMap().get("ATable").getColumnsMap().get("Description").getComments())
				.isEqualTo("ABC_TEST Schema dbo ATable Column comment");
		softAssertions
				.assertThat(abc_test_a.getTablesMap().get("ATable").getColumnsMap().get("Description").getComments())
				.isEqualTo("ABC_TEST Schema A ATable Column comment");
		softAssertions
				.assertThat(abc_test_b.getTablesMap().get("ATable").getColumnsMap().get("Description").getComments())
				.isEqualTo("ABC_TEST Schema B ATable Column comment");
		softAssertions
				.assertThat(abc_test_c.getTablesMap().get("ATable").getColumnsMap().get("Description").getComments())
				.isEqualTo("ABC_TEST Schema C ATable Column comment");

		softAssertions.assertThat(def_dbo.getTablesMap().get("ATable").getColumnsMap().get("Description").getComments())
				.isEqualTo("DEF Schema dbo ATable Column comment");
		softAssertions.assertThat(def_d.getTablesMap().get("ATable").getColumnsMap().get("Description").getComments())
				.isEqualTo("DEF Schema D ATable Column comment");
		softAssertions.assertThat(def_e.getTablesMap().get("ATable").getColumnsMap().get("Description").getComments())
				.isEqualTo("DEF Schema E ATable Column comment");
		softAssertions.assertThat(def_f.getTablesMap().get("ATable").getColumnsMap().get("Description").getComments())
				.isEqualTo("DEF Schema F ATable Column comment");
		softAssertions.assertAll();
	}

}
