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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import org.schemaspy.Main;
import org.schemaspy.testing.HtmlOutputValidator;
import org.schemaspy.testing.XmlOutputDiff;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MSSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.schemaspy.integrationtesting.MssqlServerSuite.IMAGE_NAME;
import static org.schemaspy.testing.SchemaSpyRunnerFixture.schemaSpyRunner;

/**
 * @author Nils Petzaell
 */
@DisabledOnOs(value = OS.MAC, architectures = {"aarch64"})
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Main.class)
@DirtiesContext
@Testcontainers(disabledWithoutDocker = true)
public class MSSQLServerHTMLIT {

    private static final URL expectedXML = MSSQLServerHTMLIT.class.getResource("/integrationTesting/mssqlserver/expecting/mssqlserverhtmlit/htmlit.htmlit.xml");
    private static final URL expectedDeletionOrder = MSSQLServerHTMLIT.class.getResource("/integrationTesting/mssqlserver/expecting/mssqlserverhtmlit/deletionOrder.txt");
    private static final URL expectedInsertionOrder = MSSQLServerHTMLIT.class.getResource("/integrationTesting/mssqlserver/expecting/mssqlserverhtmlit/insertionOrder.txt");

    @Container
    public static MSSQLContainer mssqlContainer =
            new MSSQLContainer(IMAGE_NAME)
                    .withInitScript("integrationTesting/mssqlserver/dbScripts/htmlit.sql");

    private static final AtomicBoolean shouldRun = new AtomicBoolean(true);

    @BeforeEach
    public void generateHTML() {
        if (shouldRun.get()) {
            String[] args = new String[]{
                    "-t", "mssql17",
                    "-db", "htmlit",
                    "-s", "htmlit",
                    "-cat", "htmlit",
                    "-host", mssqlContainer.getHost() + ":" + mssqlContainer.getMappedPort(1433),
                    "-port", String.valueOf(mssqlContainer.getMappedPort(1433)),
                    "-u", mssqlContainer.getUsername(),
                    "-p", mssqlContainer.getPassword(),
                    "-o", "target/testout/integrationtesting/mssql/html"
            };
            schemaSpyRunner(args).run();
            shouldRun.set(false);
        }
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
        assertThat(Files.newInputStream(Paths.get("target/testout/integrationtesting/mssql/html/deletionOrder.txt"), StandardOpenOption.READ)).hasSameContentAs(expectedDeletionOrder.openStream());
    }

    @Test
    void verifyInsertionOrder() throws IOException {
        assertThat(Files.newInputStream(Paths.get("target/testout/integrationtesting/mssql/html/insertionOrder.txt"), StandardOpenOption.READ)).hasSameContentAs(expectedInsertionOrder.openStream());
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
