/*
 * Copyright (C) 2017, 2018 Nils Petzaell
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
import org.junit.Before;
import org.junit.ClassRule;
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
import org.schemaspy.integrationtesting.MysqlSuite;
import org.schemaspy.output.xml.dom.XmlProducerUsingDOM;
import org.schemaspy.testing.SuiteOrTestJdbcContainerRule;
import org.schemaspy.testing.XmlOutputDiff;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testcontainers.containers.MySQLContainer;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Nils Petzaell
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Main.class)
@DirtiesContext
public class MysqlXMLIT {

    private static final Path outputPath = Paths.get("target","testout","integrationtesting","mysql","xml");

    private static final URL expectedXML = MysqlXMLIT.class.getResource("/integrationTesting/mysql/expecting/mysqlxmlit/xmlit.xmlit.xml");
    private static final URL expectedDeletionOrder = MysqlXMLIT.class.getResource("/integrationTesting/mysql/expecting/mysqlxmlit/deletionOrder.txt");
    private static final URL expectedInsertionOrder = MysqlXMLIT.class.getResource("/integrationTesting/mysql/expecting/mysqlxmlit/insertionOrder.txt");

    @SuppressWarnings("unchecked")
    @ClassRule
    public static JdbcContainerRule<MySQLContainer<?>> jdbcContainerRule =
            new SuiteOrTestJdbcContainerRule<MySQLContainer<?>>(
                    MysqlSuite.jdbcContainerRule,
                    new JdbcContainerRule<MySQLContainer<?>>(() -> new MySQLContainer<>("mysql:8-oracle"))
                            .assumeDockerIsPresent().withAssumptions(assumeDriverIsPresent())
                            .withQueryString("?useSSL=false&allowPublicKeyRetrieval=true")
                            .withInitScript("integrationTesting/mysql/dbScripts/xmlit.sql")
                            .withInitUser("root", "test")
            );

    @Autowired
    private CommandLineArgumentParser commandLineArgumentParser;

    private SchemaSpyRunner schemaSpyRunner(String...args) {
        CommandLineArguments commandLineArguments = commandLineArgumentParser.parse(args);
        SqlService sqlService = new SqlService();
        return new SchemaSpyRunner(
                new SchemaAnalyzer(
                        sqlService,
                        new DatabaseServiceFactory(sqlService),
                        commandLineArguments,
                        new XmlProducerUsingDOM(),
                        new LayoutFolder(SchemaAnalyzer.class.getClassLoader())
                ),
                commandLineArguments
        );
    }

    private static final AtomicBoolean shouldRun = new AtomicBoolean(true);

    @Before
    public void generateXML() {
        if (shouldRun.get()) {
            MySQLContainer<?> container = jdbcContainerRule.getContainer();
            String[] args = new String[]{
                    "-t", "mysql",
                    "-db", "xmlit",
                    "-s", "xmlit",
                    "-host", container.getHost() + ":" + container.getMappedPort(3306),
                    "-port", String.valueOf(container.getMappedPort(3306)),
                    "-u", container.getUsername(),
                    "-p", container.getPassword(),
                    "-nohtml",
                    "-o", outputPath.toString(),
                    "-connprops", "useSSL\\=false;allowPublicKeyRetrieval\\=true"
            };
            schemaSpyRunner(args).run();
            shouldRun.set(false);
        }
    }

    @Test
    public void verifyXML() {
        Diff d = XmlOutputDiff.diffXmlOutput(
                Input.fromFile(outputPath.resolve("xmlit.xmlit.xml").toString()),
                Input.fromURL(expectedXML)
                );
        assertThat(d.getDifferences()).isEmpty();
    }

    @Test
    public void verifyDeletionOrder() throws IOException {
        assertThat(Files.newInputStream(outputPath.resolve("deletionOrder.txt"), StandardOpenOption.READ)).hasSameContentAs(expectedDeletionOrder.openStream());
    }

    @Test
    public void verifyInsertionOrder() throws IOException {
        assertThat(Files.newInputStream(outputPath.resolve("insertionOrder.txt"), StandardOpenOption.READ)).hasSameContentAs(expectedInsertionOrder.openStream());
    }
}
