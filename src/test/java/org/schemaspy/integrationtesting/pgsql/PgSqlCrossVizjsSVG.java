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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.schemaspy.cli.SchemaSpyRunner;
import org.schemaspy.integrationtesting.PgSqlSuite;
import org.schemaspy.testing.SQLScriptsRunner;
import org.schemaspy.testing.SuiteOrTestJdbcContainerRule;
import org.testcontainers.containers.PostgreSQLContainer;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;

/**
 * @author Nils Petzaell
 */
@Ignore
public class PgSqlCrossVizjsSVG {

	private Path outputPath = Paths.get("target", "testout", "integrationtesting", "pgsql", "cross_vizjs_svg");

	@SuppressWarnings("unchecked")
	@ClassRule
	public static JdbcContainerRule<PostgreSQLContainer<?>> jdbcContainerRule = new SuiteOrTestJdbcContainerRule<PostgreSQLContainer<?>>(
			PgSqlSuite.jdbcContainerRule,
			new JdbcContainerRule<PostgreSQLContainer<?>>(() -> new PostgreSQLContainer<>("postgres:10.4"))
					.assumeDockerIsPresent().withAssumptions(assumeDriverIsPresent()).withInitFunctions(
							new SQLScriptsRunner("integrationTesting/pgsql/dbScripts/cross_schema_fk.sql", "\n\n\n")));

	private SchemaSpyRunner schemaSpyRunner = new SchemaSpyRunner();

	private static final AtomicBoolean shouldRun = new AtomicBoolean(true);

	@Before
	public synchronized void generateHTML() throws Exception {
		if (shouldRun.get()) {
			String[] args = { "-t", "pgsql", "-db", "test", "-all", "-schemaSpec", "sch(a|b)", "-vizjs", "-cat", "%",
					"-o", outputPath.toString(), "-u", "test", "-p", "test", "-host",
					jdbcContainerRule.getContainer().getContainerIpAddress(), "-port",
					jdbcContainerRule.getContainer().getMappedPort(5432).toString() };
			schemaSpyRunner.run(args);
			shouldRun.set(false);
		}
	}

	@Test
	public void verifyDiagramContent() {

	}
}
