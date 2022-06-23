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

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.schemaspy.Config;
import org.schemaspy.IntegrationTestFixture;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.input.dbms.service.DatabaseServiceFactory;
import org.schemaspy.input.dbms.service.SqlService;
import org.schemaspy.integrationtesting.PgSqlSuite;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.testing.SQLScriptsRunner;
import org.schemaspy.testing.SuiteOrTestJdbcContainerRule;
import org.testcontainers.containers.PostgreSQLContainer;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;

/**
 * @author Nils Petzaell
 */
public class PgSqlRelationshipErrorIT {

	private static final Path outputPath = Paths.get("target", "testout", "integrationtesting", "pgsql",
			"relationship_error");

	@SuppressWarnings("unchecked")
	@ClassRule
	public static JdbcContainerRule<PostgreSQLContainer<?>> jdbcContainerRule = new SuiteOrTestJdbcContainerRule<PostgreSQLContainer<?>>(
			PgSqlSuite.jdbcContainerRule,
			new JdbcContainerRule<PostgreSQLContainer<?>>(() -> new PostgreSQLContainer<>("postgres:10.4"))
					.assumeDockerIsPresent().withAssumptions(assumeDriverIsPresent())
					.withInitFunctions(new SQLScriptsRunner("integrationTesting/pgsql/dbScripts/relationship_error.sql",
							"\n\n\n")));

	@Mock
	private ProgressListener progressListener;

	private IntegrationTestFixture fixture;

	private static Database database;

	@Before
	public synchronized void createDatabaseRepresentation() throws SQLException, IOException {
		MockitoAnnotations.openMocks(this);
		if (database == null) {
			doCreateDatabaseRepresentation();
		}
	}

	private void doCreateDatabaseRepresentation() throws SQLException, IOException {
		String[] args = { "-t", "pgsql", "-db", "test", "-cat", "test", "-s", "org_cendra_person", "-o",
				outputPath.toString(), "-u", "test", "-p", "test", "-host",
				jdbcContainerRule.getContainer().getContainerIpAddress(), "-port",
				jdbcContainerRule.getContainer().getMappedPort(5432).toString() };
		fixture = IntegrationTestFixture.fromArgs(args);
		CommandLineArguments arguments = fixture.commandLineArguments();
		final SqlService sqlService = fixture.sqlService();

		Config config = new Config(args);
		sqlService.connect(config);
		Database database = new Database(sqlService.getDbmsMeta(), arguments.getDatabaseName(), arguments.getCatalog(),
				arguments.getSchema());
		new DatabaseServiceFactory(sqlService).simple(config).gatherSchemaDetails(database, null, progressListener);
		PgSqlRelationshipErrorIT.database = database;
	}

	@Test
	public void onlyHasSingleRemoteTable() {
		assertThat(database.getRemoteTables().size()).isEqualTo(1);
	}
}
