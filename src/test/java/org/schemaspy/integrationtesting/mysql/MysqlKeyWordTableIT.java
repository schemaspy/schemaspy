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

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.schemaspy.integrationtesting.MysqlSuite;
import org.schemaspy.integrationtesting.TestServiceFixture;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.model.Table;
import org.schemaspy.testing.SuiteOrTestJdbcContainerRule;
import org.testcontainers.containers.MySQLContainer;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;

/**
 * @author Nils Petzaell
 */
public class MysqlKeyWordTableIT {
	private final TestServiceFixture serviceFixture = new TestServiceFixture();

	private static final Path outputPath = Paths.get("target", "testout", "integrationtesting", "mysql", "keywords");

	@Mock
	private ProgressListener progressListener;

	private static Database database;

	@SuppressWarnings("unchecked")
	@ClassRule
	public static JdbcContainerRule<MySQLContainer<?>> jdbcContainerRule = new SuiteOrTestJdbcContainerRule<MySQLContainer<?>>(
			MysqlSuite.jdbcContainerRule,
			new JdbcContainerRule<MySQLContainer<?>>(() -> new MySQLContainer<>("mysql:5")).assumeDockerIsPresent()
					.withAssumptions(assumeDriverIsPresent()).withQueryString("?useSSL=false")
					.withInitScript("integrationTesting/mysql/dbScripts/keywordtableit.sql")
					.withInitUser("root", "test"));

	@Before
	public synchronized void createDatabaseRepresentation()
			throws SQLException, IOException, ScriptException, URISyntaxException {
		MockitoAnnotations.openMocks(this);
		if (database == null) {
			doCreateDatabaseRepresentation();
		}
	}

	private void doCreateDatabaseRepresentation() throws SQLException, IOException {
		String[] args = { "-t", "mysql", "-db", "keywordtableit", "-s", "keywordtableit", "-u", "test", "-p", "test",
				"-cat", "%", "-host", jdbcContainerRule.getContainer().getContainerIpAddress(), "-port",
				jdbcContainerRule.getContainer().getMappedPort(3306).toString(), "-o", outputPath.toString(),
				"-connprops", "useSSL\\=false" };
		final CommandLineArguments arguments = Arguments.parseArguments(args);
		Config config = new Config(args);
		serviceFixture.sqlService().connect(config);
		Database database = new Database(serviceFixture.sqlService().getDbmsMeta(), arguments.getDatabaseName(),
				arguments.getCatalog(), arguments.getSchema());
		serviceFixture.databaseService().gatherSchemaDetails(config, database, null, progressListener);
		MysqlKeyWordTableIT.database = database;
	}

	@Test
	public void hasATableNamedDistinct() {
		assertThat(database.getTables()).extracting(Table::getName).contains("DISTINCT");
	}
}
