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
package org.schemaspy.integrationtesting.informix;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.sql.SQLException;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.schemaspy.Config;
import org.schemaspy.IntegrationTestFixture;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.input.dbms.service.DatabaseServiceFactory;
import org.schemaspy.input.dbms.service.SqlService;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.schemaspy.model.TableIndex;
import org.schemaspy.testing.AssumeClassIsPresentRule;
import org.testcontainers.containers.InformixContainer;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;

/**
 * @author Nils Petzaell
 */
public class InformixIndexIT {
	private IntegrationTestFixture fixture;

	@Mock
	private ProgressListener progressListener;

	private static Database database;

	public static TestRule jdbcDriverClassPresentRule = new AssumeClassIsPresentRule("com.informix.jdbc.IfxDriver");

	@SuppressWarnings("unchecked")
	public static JdbcContainerRule<InformixContainer<?>> jdbcContainerRule = new JdbcContainerRule<>(
			(Supplier<InformixContainer<?>>) InformixContainer::new).assumeDockerIsPresent()
					.withAssumptions(assumeDriverIsPresent())
					.withInitScript("integrationTesting/informix/dbScripts/informix.sql");

	@ClassRule
	public static final TestRule chain = RuleChain.outerRule(jdbcContainerRule).around(jdbcDriverClassPresentRule);

	@Before
	public synchronized void gatheringSchemaDetailsTest() throws SQLException, IOException {
		MockitoAnnotations.openMocks(this);
		if (database == null) {
			createDatabaseRepresentation();
		}
	}

	private void createDatabaseRepresentation() throws SQLException, IOException {
		String[] args = { "-t", "informix", "-db", "test", "-s", "informix", "-cat", "test", "-server", "dev", "-o",
				"target/testout/integrationtesting/informix/index", "-u",
				jdbcContainerRule.getContainer().getUsername(), "-p", jdbcContainerRule.getContainer().getPassword(),
				"-host", jdbcContainerRule.getContainer().getContainerIpAddress(), "-port",
				jdbcContainerRule.getContainer().getJdbcPort().toString() };
		fixture = IntegrationTestFixture.fromArgs(args);
		CommandLineArguments arguments = fixture.commandLineArguments();
		final SqlService sqlService = fixture.sqlService();

		Config config = new Config(args);
		sqlService.connect(config);
		Database database = new Database(sqlService.getDbmsMeta(), arguments.getDatabaseName(), arguments.getCatalog(),
				arguments.getSchema());
		new DatabaseServiceFactory(sqlService).simple(config).gatherSchemaDetails(database, null, progressListener);
		InformixIndexIT.database = database;
	}

	@Test
	public void databaseShouldBePopulatedWithTableTest() {
		Table table = getTable("test");
		assertThat(table).isNotNull();
	}

	@Test
	public void databaseShouldBePopulatedWithTableTestAndHaveColumnName() {
		Table table = getTable("test");
		TableColumn column = table.getColumn("firstname");
		assertThat(column).isNotNull();
	}

	@Test
	public void tableTestShouldHaveTwoIndexes() {
		Table table = getTable("test");
		assertThat(table.getIndexes().size()).isEqualTo(2);
	}

	@Test
	public void tableTestIndex_test_index_shouldHaveThreeColumns() {
		TableIndex index = getTable("test").getIndex("test_index");
		assertThat(index.getColumns().size()).isEqualTo(3);
	}

	private Table getTable(String tableName) {
		return database.getTablesMap().get(tableName);
	}
}
