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
package org.schemaspy.integrationtesting.mysql;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.schemaspy.cli.SchemaSpyRunner;
import org.schemaspy.integrationtesting.MysqlSuite;
import org.schemaspy.testing.HtmlOutputValidator;
import org.schemaspy.testing.SuiteOrTestJdbcContainerRule;
import org.schemaspy.testing.XmlOutputDiff;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.MySQLContainer;
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

/**
 * @author Nils Petzaell
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class MysqlIllegalFileNameCharsInTableNameIT {

    private static URL expectedXML = MysqlIllegalFileNameCharsInTableNameIT.class.getResource("/integrationTesting/mysql/expecting/mysqlhtmlillegal/illegal.illegal.xml");
    private static URL expectedDeletionOrder = MysqlIllegalFileNameCharsInTableNameIT.class.getResource("/integrationTesting/mysql/expecting/mysqlhtmlillegal/deletionOrder.txt");
    private static URL expectedInsertionOrder = MysqlIllegalFileNameCharsInTableNameIT.class.getResource("/integrationTesting/mysql/expecting/mysqlhtmlillegal/insertionOrder.txt");

    @ClassRule
    public static JdbcContainerRule<MySQLContainer> jdbcContainerRule =
            new SuiteOrTestJdbcContainerRule<>(
                    MysqlSuite.jdbcContainerRule,
                    new JdbcContainerRule<MySQLContainer>(() -> new MySQLContainer<>("mysql:5").withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci"))
                        .assumeDockerIsPresent().withAssumptions(assumeDriverIsPresent())
                        .withQueryString("?useSSL=false")
                        .withInitScript("integrationTesting/mysql/dbScripts/filenameillegal.sql")
                        .withInitUser("root", "test")
            );

    @Autowired
    private SchemaSpyRunner schemaSpyRunner;

    private static final AtomicBoolean shouldRun = new AtomicBoolean(true);

    @Before
    public synchronized void generateHTML() throws Exception {
        if (shouldRun.get()) {
            String[] args = new String[]{
                    "-t", "mysql",
                    "-db", "illegal",
                    "-s", "illegal",
                    "-host", jdbcContainerRule.getContainer().getContainerIpAddress() + ":" + String.valueOf(jdbcContainerRule.getContainer().getMappedPort(3306)),
                    "-port", String.valueOf(jdbcContainerRule.getContainer().getMappedPort(3306)),
                    "-u", jdbcContainerRule.getContainer().getUsername(),
                    "-p", jdbcContainerRule.getContainer().getPassword(),
                    "-o", "target/mysqlhtmlillegal",
                    "-connprops", "useSSL\\=false"
            };
            schemaSpyRunner.run(args);
            shouldRun.set(false);
        }
    }

    @Test
    public void verifyXML() {
        Diff d = XmlOutputDiff.diffXmlOutput(
                Input.fromFile("target/mysqlhtmlillegal/illegal.illegal.xml"),
                Input.fromURL(expectedXML)
        );
        assertThat(d.getDifferences()).isEmpty();
    }

    @Test
    public void verifyDeletionOrder() throws IOException {
        assertThat(Files.newInputStream(Paths.get("target/mysqlhtmlillegal/deletionOrder.txt"), StandardOpenOption.READ)).hasSameContentAs(expectedDeletionOrder.openStream());
    }

    @Test
    public void verifyInsertionOrder() throws IOException {
        assertThat(Files.newInputStream(Paths.get("target/mysqlhtmlillegal/insertionOrder.txt"), StandardOpenOption.READ)).hasSameContentAs(expectedInsertionOrder.openStream());
    }

    @Test
    public void producesSameContent() throws IOException {
        SoftAssertions softAssertions = HtmlOutputValidator
                .hasProducedValidOutput(
                        Paths.get("target","mysqlhtmlillegal"),
                        Paths.get("src","test","resources","integrationTesting","mysql","expecting","mysqlhtmlillegal")
                        );
        softAssertions.assertThat(softAssertions.wasSuccess()).isTrue();
        softAssertions.assertAll();
    }
}
