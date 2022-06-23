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
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.schemaspy.Config;
import org.schemaspy.IntegrationTestFixture;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.input.dbms.service.DatabaseServiceFactory;
import org.schemaspy.input.dbms.service.SqlService;
import org.schemaspy.integrationtesting.MysqlSuite;
import org.schemaspy.model.Database;
import org.schemaspy.model.ForeignKeyConstraint;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.schemaspy.model.TableIndex;
import org.schemaspy.testing.SuiteOrTestJdbcContainerRule;
import org.testcontainers.containers.MySQLContainer;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;

/**
 * @author Nils Petzaell
 */
@Ignore
/*
 * https://github.com/schemaspy/schemaspy/pull/174#issuecomment-352158979
 * Summary: mysql-connector-java has a bug regarding dots in tablePattern.
 * https://bugs.mysql.com/bug.php?id=63992
 */
public class MysqlSpacesIT {

	private static final Path outputPath = Paths.get("target", "testout", "integrationtesting", "mysql", "spaces");

	@Mock
	private ProgressListener progressListener;

	private IntegrationTestFixture fixture;

	private static Database database;

	@SuppressWarnings("unchecked")
	@ClassRule
	public static JdbcContainerRule<MySQLContainer<?>> jdbcContainerRule = new SuiteOrTestJdbcContainerRule<MySQLContainer<?>>(
			MysqlSuite.jdbcContainerRule,
			new JdbcContainerRule<MySQLContainer<?>>(() -> new MySQLContainer<>("mysql:5")).assumeDockerIsPresent()
					.withAssumptions(assumeDriverIsPresent()).withQueryString("?useSSL=false")
					.withInitScript("integrationTesting/mysql/dbScripts/spacesit.sql_ignore")
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
		String[] args = { "-t", "mysql", "-db", "TEST 1.0", "-s", "TEST 1.0", "-cat", "%", "-u", "test", "-p", "test",
				"-host", jdbcContainerRule.getContainer().getContainerIpAddress(), "-port",
				jdbcContainerRule.getContainer().getMappedPort(3306).toString(), "-o", outputPath.toString(),
				"-connprops", "useSSL\\=false" };
		fixture = IntegrationTestFixture.fromArgs(args);
		CommandLineArguments arguments = fixture.commandLineArguments();
		final SqlService sqlService = fixture.sqlService();

		Config config = new Config(args);
		sqlService.connect(config);
		Database database = new Database(sqlService.getDbmsMeta(), arguments.getDatabaseName(), arguments.getCatalog(),
				arguments.getSchema());
		new DatabaseServiceFactory(sqlService).simple(config).gatherSchemaDetails(database, null, progressListener);
		MysqlSpacesIT.database = database;
	}

	@Test
	public void databaseShouldExist() {
		assertThat(database).isNotNull();
		assertThat(database.getName()).isEqualToIgnoringCase("TEST 1.0");
	}

	@Test
	public void databaseShouldHaveTable() {
		assertThat(database.getTables()).extracting(Table::getName).contains("TABLE 1.0");
	}

	@Test
	public void tableShouldHavePKWithAutoIncrement() {
		assertThat(database.getTablesMap().get("TABLE 1.0").getColumns()).extracting(TableColumn::getName)
				.contains("id");
		assertThat(database.getTablesMap().get("TABLE 1.0").getColumn("id").isPrimary()).isTrue();
		assertThat(database.getTablesMap().get("TABLE 1.0").getColumn("id").isAutoUpdated()).isTrue();
	}

	@Test
	public void tableShouldHaveForeignKey() {
		assertThat(database.getTablesMap().get("TABLE 1.0").getForeignKeys()).extracting(ForeignKeyConstraint::getName)
				.contains("link fk");
	}

	@Test
	public void tableShouldHaveUniqueKey() {
		assertThat(database.getTablesMap().get("TABLE 1.0").getIndexes()).extracting(TableIndex::getName)
				.contains("name_link_unique");
	}

	@Test
	public void tableShouldHaveColumnWithSpaceInIt() {
		assertThat(database.getTablesMap().get("TABLE 1.0").getColumns()).extracting(TableColumn::getName)
				.contains("link id");
	}
}
