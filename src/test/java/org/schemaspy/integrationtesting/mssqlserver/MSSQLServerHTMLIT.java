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

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.schemaspy.cli.SchemaSpyRunner;
import org.schemaspy.integrationtesting.MssqlServerSuite;
import org.schemaspy.testing.HtmlOutputValidator;
import org.schemaspy.testing.SuiteOrTestJdbcContainerRule;
import org.schemaspy.testing.XmlOutputDiff;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.MSSQLContainer;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.schemaspy.integrationtesting.MssqlServerSuite.IMAGE_NAME;

/**
 * @author Nils Petzaell
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class MSSQLServerHTMLIT {

    private static URL expectedXML = MSSQLServerHTMLIT.class.getResource("/integrationTesting/mssqlserver/expecting/mssqlserverhtmlit/htmlit.htmlit.xml");
    private static URL expectedDeletionOrder = MSSQLServerHTMLIT.class.getResource("/integrationTesting/mssqlserver/expecting/mssqlserverhtmlit/deletionOrder.txt");
    private static URL expectedInsertionOrder = MSSQLServerHTMLIT.class.getResource("/integrationTesting/mssqlserver/expecting/mssqlserverhtmlit/insertionOrder.txt");

    @SuppressWarnings("unchecked")
    @ClassRule
    public static JdbcContainerRule<MSSQLContainer> jdbcContainerRule =
            new SuiteOrTestJdbcContainerRule<MSSQLContainer>(
                    MssqlServerSuite.jdbcContainerRule,
                    new JdbcContainerRule<>(() -> new MSSQLContainer(IMAGE_NAME))
                            .assumeDockerIsPresent()
                            .withAssumptions(assumeDriverIsPresent())
                            .withInitScript("integrationTesting/mssqlserver/dbScripts/htmlit.sql")
            );

    @Autowired
    private SchemaSpyRunner schemaSpyRunner;

    private static final AtomicBoolean shouldRun = new AtomicBoolean(true);

    @Before
    public void generateHTML() throws Exception {
        if (shouldRun.get()) {
            String[] args = new String[]{
                    "-t", "mssql17",
                    "-db", "htmlit",
                    "-s", "htmlit",
                    "-cat", "htmlit",
                    "-host", jdbcContainerRule.getContainer().getContainerIpAddress() + ":" + jdbcContainerRule.getContainer().getMappedPort(1433),
                    "-port", String.valueOf(jdbcContainerRule.getContainer().getMappedPort(1433)),
                    "-u", jdbcContainerRule.getContainer().getUsername(),
                    "-p", jdbcContainerRule.getContainer().getPassword(),
                    "-o", "target/testout/integrationtesting/mssql/html"
            };
            schemaSpyRunner.run(args);
            shouldRun.set(false);
        }
    }

    @Test
    public void verifyXML() {
        Diff d = XmlOutputDiff.diffXmlOutput(
                Input.fromFile("target/testout/integrationtesting/mssql/html/htmlit.htmlit.xml"),
                Input.fromURL(expectedXML)
        );
        assertThat(d.getDifferences()).isEmpty();
    }

    @Test
    public void verifyDeletionOrder() throws IOException {
        assertThat(Files.newInputStream(Paths.get("target/testout/integrationtesting/mssql/html/deletionOrder.txt"), StandardOpenOption.READ)).hasSameContentAs(expectedDeletionOrder.openStream());
    }

    @Test
    public void verifyInsertionOrder() throws IOException {
        assertThat(Files.newInputStream(Paths.get("target/testout/integrationtesting/mssql/html/insertionOrder.txt"), StandardOpenOption.READ)).hasSameContentAs(expectedInsertionOrder.openStream());
    }

    @Test
    public void producesSameContent() throws IOException {
        SoftAssertions softAssertions = HtmlOutputValidator
                .hasProducedValidOutput(
                        Paths.get("target","testout","integrationtesting","mssql","html"),
                        Paths.get("src", "test", "resources", "integrationTesting", "mssqlserver", "expecting", "mssqlserverhtmlit")
                );
        softAssertions.assertThat(softAssertions.wasSuccess()).isTrue();
        softAssertions.assertAll();
    }
}
