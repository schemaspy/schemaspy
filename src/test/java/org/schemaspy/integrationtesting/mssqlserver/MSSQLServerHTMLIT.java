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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SchemaSpy.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.schemaspy.integrationtesting.mssqlserver;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.schemaspy.integrationtesting.MssqlSuite;
import org.schemaspy.testing.HtmlOutputValidator;
import org.schemaspy.testing.XmlOutputDiff;
import org.schemaspy.testing.testcontainers.SuiteContainerExtension;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static org.assertj.core.api.Assertions.assertThat;
import static org.schemaspy.testing.SchemaSpyRunnerFixture.schemaSpyRunner;

/**
 * @author Nils Petzaell
 */
@DisabledOnOs(value = OS.MAC, architectures = {"aarch64"})
class MSSQLServerHTMLIT {

    private static final URL expectedXML =
            MSSQLServerHTMLIT.class.getResource("/integrationTesting/mssqlserver/expecting/mssqlserverhtmlit/htmlit.htmlit.xml");
    private static final URL expectedDeletionOrder =
            MSSQLServerHTMLIT.class.getResource("/integrationTesting/mssqlserver/expecting/mssqlserverhtmlit/deletionOrder.txt");
    private static final URL expectedInsertionOrder =
            MSSQLServerHTMLIT.class.getResource("/integrationTesting/mssqlserver/expecting/mssqlserverhtmlit/insertionOrder.txt");

    @RegisterExtension
    static SuiteContainerExtension container = MssqlSuite.SUITE_CONTAINER;

    @BeforeAll
    static void generateHTML() {
        String[] args = new String[]{
                "-t", "mssql17",
                "-db", "htmlit",
                "-s", "htmlit",
                "-cat", "htmlit",
                "-host", container.getHost() + ":" + container.getPort(1433),
                "-port", container.getPort(1433),
                "-u", container.getUsername(),
                "-p", container.getPassword(),
                "-o", "target/testout/integrationtesting/mssql/html",
                "--no-orphans"
        };
        schemaSpyRunner(args).run();
    }

    @Test
    void verifyXML() {
        Diff d = XmlOutputDiff.diffXmlOutput(
                Input.fromFile("target/testout/integrationtesting/mssql/html/htmlit.htmlit.xml"),
                Input.fromURL(expectedXML)
        );
        assertThat(d.getDifferences()).isEmpty();
    }

    @Test
    void verifyDeletionOrder() throws IOException {
        assertThat(
                Files.newInputStream(
                        Paths.get("target/testout/integrationtesting/mssql/html/deletionOrder.txt"), StandardOpenOption.READ)
        ).hasSameContentAs(expectedDeletionOrder.openStream());
    }

    @Test
    void verifyInsertionOrder() throws IOException {
        assertThat(
                Files.newInputStream(
                        Paths.get("target/testout/integrationtesting/mssql/html/insertionOrder.txt"), StandardOpenOption.READ)
        ).hasSameContentAs(expectedInsertionOrder.openStream());
    }

    @Test
    void producesSameContent() throws IOException {
        SoftAssertions softAssertions = HtmlOutputValidator
                .hasProducedValidOutput(
                        Paths.get("target","testout","integrationtesting","mssql","html"),
                        Paths.get("src", "test", "resources", "integrationTesting", "mssqlserver", "expecting", "mssqlserverhtmlit")
                );
        softAssertions.assertThat(softAssertions.wasSuccess()).isTrue();
        softAssertions.assertAll();
    }
}
