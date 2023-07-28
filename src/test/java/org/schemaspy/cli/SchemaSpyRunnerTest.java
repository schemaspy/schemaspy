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

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.schemaspy.SchemaAnalyzer;
import org.schemaspy.input.dbms.MissingParameterException;
import org.schemaspy.input.dbms.exceptions.ConnectionFailure;
import org.schemaspy.model.Database;
import org.schemaspy.model.EmptySchemaException;
import org.schemaspy.testing.logback.Logback;
import org.schemaspy.testing.logback.LogbackExtension;

import java.io.IOException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.schemaspy.testing.SchemaSpyRunnerFixture.schemaSpyRunner;

public class SchemaSpyRunnerTest {

    @RegisterExtension
    public static LogbackExtension logback = new LogbackExtension();

    private static final String[] args = {
            "-t","mysql",
            "-o","target/tmp",
            "-sso"};

    private SchemaAnalyzer schemaAnalyzer = mock(SchemaAnalyzer.class);

    @Test
    public void emptySchemaExitCode_2() throws IOException, SQLException {
        when(schemaAnalyzer.analyze()).thenThrow(new EmptySchemaException());
        assertThat(
                schemaSpyRunner(schemaAnalyzer, args)
                        .run()
        ).isEqualTo(2);
    }

    @Test
    public void connectionFailureExitCode_3() throws IOException, SQLException {
        when(schemaAnalyzer.analyze()).thenThrow(new ConnectionFailure("failed to connect"));
        assertThat(
                schemaSpyRunner(schemaAnalyzer, args)
                        .run()
        ).isEqualTo(3);
    }

    @Test
    public void returnsNoneNullExitCode_0() throws IOException, SQLException {
        Database database = mock(Database.class);
        when(schemaAnalyzer.analyze()).thenReturn(database);
        assertThat(
                schemaSpyRunner(schemaAnalyzer, args)
                        .run()
        ).isZero();
    }

    @Test
    @Logback(logger = "root")
    public void exitCode_5_withLogging() throws IOException, SQLException {
        logback.expect(Matchers.containsString("'-t mysql"));
        logback.expect(Matchers.not(Matchers.containsString("'-t mssql")));
        when(schemaAnalyzer.analyze()).thenThrow(new MissingParameterException("host", "Host is missing"));
        assertThat(
                schemaSpyRunner(schemaAnalyzer, args)
                        .run()
        ).isEqualTo(5);
    }

    @Test
    public void ioExceptionExitCode_7() throws IOException, SQLException {
        when(schemaAnalyzer.analyze()).thenThrow(new IOException("file permission error"));
        assertThat(
                schemaSpyRunner(schemaAnalyzer, args)
                        .run()
        ).isEqualTo(7);
    }
}