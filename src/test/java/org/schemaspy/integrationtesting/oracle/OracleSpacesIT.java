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
package org.schemaspy.integrationtesting.oracle;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

import javax.script.ScriptException;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.schemaspy.Config;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.integrationtesting.TestServiceFixture;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.model.Table;
import org.schemaspy.testing.AssumeClassIsPresentRule;
import org.testcontainers.containers.OracleContainer;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;

/**
 * @author Nils Petzaell
 */
public class OracleSpacesIT {

	private static final Path outputPath = Paths.get("target", "testout", "integrationtesting", "oracle", "spaces");

	private final TestServiceFixture serviceFixture = new TestServiceFixture();

	@Mock
	private ProgressListener progressListener;

	@Mock
	private CommandLineArguments arguments;

	private static Database database;

	@SuppressWarnings("unchecked")
	public static JdbcContainerRule<OracleContainer> jdbcContainerRule = new JdbcContainerRule<>(
			() -> new OracleContainer("christophesurmont/oracle-xe-11g")).assumeDockerIsPresent()
					.withAssumptions(assumeDriverIsPresent())
					.withInitScript("integrationTesting/oracle/dbScripts/spaces_in_table_names.sql");

	public static TestRule jdbcDriverClassPresentRule = new AssumeClassIsPresentRule("oracle.jdbc.OracleDriver");

	@ClassRule
	public static final TestRule chain = RuleChain.outerRule(jdbcContainerRule).around(jdbcDriverClassPresentRule);

	@Before
	public synchronized void gatheringSchemaDetailsTest()
			throws SQLException, IOException, ScriptException, URISyntaxException {
		MockitoAnnotations.openMocks(this);
		if (database == null) {
			createDatabaseRepresentation();
		}
	}

	private void createDatabaseRepresentation() throws SQLException, IOException {
		String[] args = { "-t", "orathin", "-db", jdbcContainerRule.getContainer().getSid(), "-s", "ORASPACEIT", "-cat",
				"%", "-o", outputPath.toString(), "-u", "oraspaceit", "-p", "oraspaceit123", "-host",
				jdbcContainerRule.getContainer().getContainerIpAddress(), "-port",
				jdbcContainerRule.getContainer().getOraclePort().toString() };
		given(arguments.getOutputDirectory()).willReturn(outputPath.toFile());
		given(arguments.getDatabaseType()).willReturn("orathin");
		given(arguments.getUser()).willReturn("orait");
		given(arguments.getSchema()).willReturn("ORASPACEIT");
		given(arguments.getCatalog()).willReturn("%");
		given(arguments.getDatabaseName()).willReturn(jdbcContainerRule.getContainer().getSid());
		Config config = new Config(args);
		serviceFixture.sqlService().connect(config);
		Database database = new Database(serviceFixture.sqlService().getDbmsMeta(), arguments.getDatabaseName(),
				arguments.getCatalog(), arguments.getSchema());
		serviceFixture.databaseService().gatherSchemaDetails(config, database, null, progressListener);
		OracleSpacesIT.database = database;
	}

	@Test
	public void databaseShouldHaveTableWithSpaces() {
		assertThat(database.getTables()).extracting(Table::getName).contains("test 1.0");
	}
}
