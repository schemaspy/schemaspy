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
package org.schemaspy.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.Test;
import org.mockito.Mockito;
import org.schemaspy.Config;
import org.schemaspy.SchemaAnalyzer;
import org.schemaspy.cli.SchemaSpyRunner.ExitCode;
import org.schemaspy.input.dbms.exceptions.ConnectionFailure;
import org.schemaspy.input.dbms.service.DatabaseService;
import org.schemaspy.input.dbms.service.SqlService;
import org.schemaspy.model.Database;
import org.schemaspy.model.EmptySchemaException;
import org.schemaspy.model.InvalidConfigurationException;

public class SchemaSpyRunnerTest {

	private static final String[] ARGS = { "-t", "mysql", "-o", "target/tmp", "-sso" };

	@Test
	public void ioExceptionExitCode() throws IOException, SQLException {
		final SchemaSpyRunner runner = createRunnerThrowing(new IOException("file permission error"));

		assertThat(runner.run(ARGS)).isEqualTo(ExitCode.GENERIC_ERROR);
	}

	@Test
	public void sqlExceptionExitCode() throws IOException, SQLException {
		final SchemaSpyRunner runner = createRunnerThrowing(new SQLException("thou shalt not query"));

		assertThat(runner.run(ARGS)).isEqualTo(ExitCode.GENERIC_ERROR);
	}

	@Test
	public void invalidConfigExceptionExitCode() throws IOException, SQLException {
		final SchemaSpyRunner runner = createRunnerThrowing(new InvalidConfigurationException("offensive sh*t"));

		assertThat(runner.run(ARGS)).isEqualTo(ExitCode.CONFIG_ERROR);
	}

	@Test
	public void emptySchemaExitCode() throws IOException, SQLException {
		final SchemaSpyRunner runner = createRunnerThrowing(new EmptySchemaException());

		assertThat(runner.run(ARGS)).isEqualTo(ExitCode.EMPTY_SCHEMA);
	}

	@Test
	public void connectionFailureExitCode() throws IOException, SQLException {
		final ConnectionFailure ex = new ConnectionFailure("failed to connect");
		final SchemaSpyRunner runner = createRunnerThrowing(ex);

		assertThat(runner.run(ARGS)).isEqualTo(ExitCode.CONNECTION_ERROR);
	}

	@Test
	public void returnsNoneNullExitCode() throws IOException, SQLException {
		final Database database = mock(Database.class);
		final SchemaAnalyzer schemaAnalyzer = Mockito.mock(SchemaAnalyzer.class);
		when(schemaAnalyzer.analyze(any(Config.class))).thenReturn(database);
		final SchemaSpyRunner runner = withAnalyzer(schemaAnalyzer);

		assertThat(runner.run(ARGS)).isEqualTo(ExitCode.OK);
	}

	SchemaSpyRunner createRunnerThrowing(final Exception ex) throws SQLException, IOException {
		final SchemaAnalyzer schemaAnalyzer = Mockito.mock(SchemaAnalyzer.class);
		when(schemaAnalyzer.analyze(any(Config.class))).thenThrow(ex);
		final SchemaSpyRunner runner = withAnalyzer(schemaAnalyzer);
		return runner;
	}

	private SchemaSpyRunner withAnalyzer(SchemaAnalyzer analyzer) {
		return new SchemaSpyRunner() {
			@Override
			SchemaAnalyzer createAnalzyer(SqlService sqlService, DatabaseService databaseService,
					CommandLineArguments arguments) {
				return analyzer;
			}
		};
	}
}