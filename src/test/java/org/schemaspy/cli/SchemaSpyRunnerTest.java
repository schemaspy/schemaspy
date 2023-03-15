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
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.schemaspy.Config;
import org.schemaspy.SchemaAnalyzer;
import org.schemaspy.input.dbms.MissingParameterException;
import org.schemaspy.input.dbms.exceptions.ConnectionFailure;
import org.schemaspy.model.Database;
import org.schemaspy.model.EmptySchemaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.OutputCaptureRule;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SchemaSpyRunnerTest {

    @Rule
    public OutputCaptureRule outputCapture = new OutputCaptureRule();

    private static final String[] args = {
            "-t","mysql",
            "-o","target/tmp",
            "-sso"};

    @MockBean
    private SchemaAnalyzer schemaAnalyzer;

    @Autowired
    private SchemaSpyRunner schemaSpyRunner;

    @Test
    @DirtiesContext
    public void ioExceptionExitCode_1() throws IOException, SQLException {
        when(schemaAnalyzer.analyze(any(Config.class))).thenThrow(new IOException("file permission error"));
        schemaSpyRunner.run(args);
        assertThat(schemaSpyRunner.getExitCode()).isEqualTo(1);
    }

    @Test
    @DirtiesContext
    public void emptySchemaExitCode_2() throws IOException, SQLException {
        when(schemaAnalyzer.analyze(any(Config.class))).thenThrow(new EmptySchemaException());
        schemaSpyRunner.run(args);
        assertThat(schemaSpyRunner.getExitCode()).isEqualTo(2);
    }

    @Test
    @DirtiesContext
    public void connectionFailureExitCode_3() throws IOException, SQLException {
        when(schemaAnalyzer.analyze(any(Config.class))).thenThrow(new ConnectionFailure("failed to connect"));
        schemaSpyRunner.run(args);
        assertThat(schemaSpyRunner.getExitCode()).isEqualTo(3);
    }

    @Test
    @DirtiesContext
    public void returnsNoneNullExitCode_0() throws IOException, SQLException {
        Database database = mock(Database.class);
        when(schemaAnalyzer.analyze(any(Config.class))).thenReturn(database);
        schemaSpyRunner.run(args);
        assertThat(schemaSpyRunner.getExitCode()).isEqualTo(0);
    }

    @Test
    @DirtiesContext
    public void exitCode_5_withLogging() throws IOException, SQLException {
        outputCapture.expect(Matchers.containsString("'-t mysql"));
        outputCapture.expect(Matchers.not(Matchers.containsString("'-t mssql")));
        when(schemaAnalyzer.analyze(any(Config.class))).thenThrow(new MissingParameterException("host", "Host is missing"));
        schemaSpyRunner.run(args);
        assertThat(schemaSpyRunner.getExitCode()).isEqualTo(5);
    }
}