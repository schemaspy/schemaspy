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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.schemaspy.Config;
import org.schemaspy.SchemaAnalyzer;
import org.schemaspy.input.dbms.exceptions.ConnectionFailure;
import org.schemaspy.model.Database;
import org.schemaspy.model.EmptySchemaException;

public class SchemaSpyRunnerTest {

	private static final String[] args = { "-t", "mysql", "-o", "target/tmp", "-sso" };

	@Mock
	private SchemaAnalyzer schemaAnalyzer;

	@Before
	public void initMocks() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void ioExceptionExitCode_1() throws IOException, SQLException {
		final SchemaSpyRunner schemaSpyRunner = createRunner();
		when(schemaAnalyzer.analyze(any(Config.class))).thenThrow(new IOException("file permission error"));
		schemaSpyRunner.run(args);
		assertThat(schemaSpyRunner.getExitCode()).isEqualTo(1);
	}

	@Test
	public void emptySchemaExitCode_2() throws IOException, SQLException {
		final SchemaSpyRunner schemaSpyRunner = createRunner();
		when(schemaAnalyzer.analyze(any(Config.class))).thenThrow(new EmptySchemaException());
		schemaSpyRunner.run(args);
		assertThat(schemaSpyRunner.getExitCode()).isEqualTo(2);
	}

	@Test
	public void connectionFailureExitCode_3() throws IOException, SQLException {
		final SchemaSpyRunner schemaSpyRunner = createRunner();
		when(schemaAnalyzer.analyze(any(Config.class))).thenThrow(new ConnectionFailure("failed to connect"));
		schemaSpyRunner.run(args);
		assertThat(schemaSpyRunner.getExitCode()).isEqualTo(3);
	}

	@Test
	public void returnsNoneNullExitCode_0() throws IOException, SQLException {
		final SchemaSpyRunner schemaSpyRunner = createRunner();
		Database database = mock(Database.class);
		when(schemaAnalyzer.analyze(any(Config.class))).thenReturn(database);
		schemaSpyRunner.run(args);
		assertThat(schemaSpyRunner.getExitCode()).isEqualTo(0);
	}

	private SchemaSpyRunner createRunner() {
		return new SchemaSpyRunner(ctx -> schemaAnalyzer);
	}
}