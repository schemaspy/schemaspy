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
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.integrationtesting.Arguments;
import org.schemaspy.integrationtesting.TestServiceFixture;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.model.Table;
import org.schemaspy.testing.AssumeClassIsPresentRule;
import org.schemaspy.testing.SQLScriptsRunner;
import org.testcontainers.containers.InformixContainer;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;

/**
 * @author Nils Petzaell
 */
public class InformixRoutinesIT {
	private final TestServiceFixture serviceFixture = new TestServiceFixture();

	@Mock
	private ProgressListener progressListener;

	private static Database database;

	public static TestRule jdbcDriverClassPresentRule = new AssumeClassIsPresentRule("com.informix.jdbc.IfxDriver");

	@SuppressWarnings("unchecked")
	public static JdbcContainerRule<InformixContainer<?>> jdbcContainerRule = new JdbcContainerRule<>(
			(Supplier<InformixContainer<?>>) InformixContainer::new).assumeDockerIsPresent()
					.withAssumptions(assumeDriverIsPresent()).withInitFunctions(new SQLScriptsRunner(
							"integrationTesting/informix/dbScripts/informixroutines.sql", "\n\n\n"));

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
				"target/testout/integrationtesting/informix/routines", "-u",
				jdbcContainerRule.getContainer().getUsername(), "-p", jdbcContainerRule.getContainer().getPassword(),
				"-host", jdbcContainerRule.getContainer().getContainerIpAddress(), "-port",
				jdbcContainerRule.getContainer().getJdbcPort().toString() };
		final CommandLineArguments arguments = Arguments.parseArguments(args);
		Config config = new Config(args);
		serviceFixture.sqlService().connect(config);
		Database database = new Database(serviceFixture.sqlService().getDbmsMeta(), arguments.getDatabaseName(),
				arguments.getCatalog(), arguments.getSchema());
		serviceFixture.databaseService().gatherSchemaDetails(config, database, null, progressListener);
		InformixRoutinesIT.database = database;
	}

	@Test
	public void databaseShouldBePopulatedWithTableTest() {
		Table table = getTable("test");
		assertThat(table).isNotNull();
	}

	@Test
	public void databaseShouldHaveCompleteRoutineDefinition() {
		String expecting = "CREATE FUNCTION gc_comb(partial1 LVARCHAR, partial2 LVARCHAR) RETURNING LVARCHAR; IF partial1 IS NULL OR partial1 = '' THEN RETURN partial2; ELIF partial2 IS NULL OR partial2 = '' THEN RETURN partial1; ELSE RETURN partial1 || ',' || partial2; END IF; END FUNCTION;";
		String actual = database.getRoutinesMap().get("gc_comb(lvarchar,lvarchar)").getDefinition().trim();
		assertThat(actual).isEqualToIgnoringCase(expecting);
	}

	private Table getTable(String tableName) {
		return database.getTablesMap().get(tableName);
	}
}
