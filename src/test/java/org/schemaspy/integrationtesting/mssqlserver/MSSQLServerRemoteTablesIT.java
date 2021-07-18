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
import static org.assertj.core.api.Assertions.assertThat;
import static org.schemaspy.integrationtesting.MssqlServerSuite.IMAGE_NAME;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.script.ScriptException;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.schemaspy.Config;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.integrationtesting.Arguments;
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
public class MSSQLServerRemoteTablesIT {
	private final TestServiceFixture serviceFixture = new TestServiceFixture();

	@Mock
	private ProgressListener progressListener;

	private static Database database;

	@SuppressWarnings("unchecked")
	@ClassRule
	public static JdbcContainerRule<MSSQLContainer> jdbcContainerRule = new SuiteOrTestJdbcContainerRule<MSSQLContainer>(
			MssqlServerSuite.jdbcContainerRule,
			new JdbcContainerRule<>(() -> new MSSQLContainer(IMAGE_NAME)).assumeDockerIsPresent()
					.withAssumptions(assumeDriverIsPresent())
					.withInitScript("integrationTesting/mssqlserver/dbScripts/mssql_remote_tables.sql"));

	@Before
	public synchronized void gatheringSchemaDetailsTest()
			throws SQLException, IOException, ScriptException, URISyntaxException {
		MockitoAnnotations.openMocks(this);
		if (database == null) {
			createDatabaseRepresentation();
		}
	}

	private void createDatabaseRepresentation() throws SQLException, IOException {
		String[] args = { "-t", "mssql17", "-db", "ACME", "-o", "target/testout/integrationtesting/mssql/remote_table",
				"-u", "schemaspy", "-p", "qwerty123!", "-host",
				jdbcContainerRule.getContainer().getContainerIpAddress(), "-port",
				jdbcContainerRule.getContainer().getMappedPort(1433).toString() };
		final CommandLineArguments arguments = Arguments.parseArguments(args);
		Config config = new Config(args);
		DatabaseMetaData databaseMetaData = serviceFixture.sqlService().connect(config);
		Database database = new Database(serviceFixture.sqlService().getDbmsMeta(), arguments.getDatabaseName(),
				databaseMetaData.getConnection().getCatalog(), databaseMetaData.getConnection().getSchema());
		serviceFixture.databaseService().gatherSchemaDetails(config, database, null, progressListener);
		MSSQLServerRemoteTablesIT.database = database;
	}

	@Test
	public void databaseShouldHaveZeroRemoteTables() {
		assertThat(database.getRemoteTables()).isEmpty();
	}
}
