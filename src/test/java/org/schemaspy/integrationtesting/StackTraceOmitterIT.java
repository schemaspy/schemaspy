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
package org.schemaspy.integrationtesting;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.schemaspy.LayoutFolder;
import org.schemaspy.Main;
import org.schemaspy.SchemaAnalyzer;
import org.schemaspy.cli.CommandLineArgumentParser;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.cli.SchemaSpyRunner;
import org.schemaspy.input.dbms.service.DatabaseServiceFactory;
import org.schemaspy.input.dbms.service.SqlService;
import org.schemaspy.output.xml.dom.XmlProducerUsingDOM;
import org.schemaspy.testing.Logger;
import org.schemaspy.testing.LoggingRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Nils Petzaell
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class)
@DirtiesContext
public class StackTraceOmitterIT {

    @Rule
    public LoggingRule loggingRule = new LoggingRule();

    @Autowired
    private SqlService sqlService;
    @Autowired
    private CommandLineArguments commandLineArguments;
    @Autowired
    private CommandLineArgumentParser commandLineArgumentParser;
    @Autowired
    private LoggingSystem loggingSystem;

    private SchemaSpyRunner schemaSpyRunner() {
        return new SchemaSpyRunner(
                new SchemaAnalyzer(
                        sqlService,
                        new DatabaseServiceFactory(sqlService),
                        commandLineArguments,
                        new XmlProducerUsingDOM(),
                        new LayoutFolder(SchemaAnalyzer.class.getClassLoader())
                ),
                commandLineArguments,
                commandLineArgumentParser,
                loggingSystem
        );
    }

    @Test
    @DirtiesContext
    @Logger(value = SchemaSpyRunner.class, pattern = "%msg%n%debugEx")
    public void noStacktraceWhenLoggingIsOf() {
        schemaSpyRunner().run("-sso","-o","target/somefolder", "-t", "doesnt-exist");
        String log = loggingRule.getLog();
        assertThat(log)
                .isNotEmpty()
                .doesNotContain("Caused by:");
    }

    @Test
    @DirtiesContext
    @Logger(value = SchemaSpyRunner.class, pattern = "%msg%n%debugEx")
    public void stacktraceWhenLoggingIsOn() {
        try {
            schemaSpyRunner().run("-sso", "-o", "target/somefolder", "-t", "doesnt-exist", "-debug");
            String log = loggingRule.getLog();
            assertThat(log)
                    .isNotEmpty()
                    .contains("Caused by:");
        } finally {
            loggingSystem.setLogLevel("org.schemaspy", LogLevel.INFO);
        }
    }
}
