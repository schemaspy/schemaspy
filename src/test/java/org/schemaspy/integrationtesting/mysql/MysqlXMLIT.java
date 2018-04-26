/*
 * Copyright (c) 2018 Nils Petzaell
 *
 * This file is part of SchemaSpy.
 *
 *  SchemaSpy is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  SchemaSpy is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.schemaspy.integrationtesting.mysql;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.schemaspy.Main;
import org.schemaspy.integrationtesting.MysqlSuite;
import org.schemaspy.testing.IgnoreUsingXPath;
import org.schemaspy.testing.SuiteOrTestJdbcContainerRule;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testcontainers.containers.MySQLContainer;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.DifferenceEvaluators;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
public class MysqlXMLIT {

    private static URL expectedXML = MysqlXMLIT.class.getResource("/integrationTesting/mysql/expecting/mysqlxmlit/xmlit.xmlit.xml");
    private static URL expectedDeletionOrder = MysqlXMLIT.class.getResource("/integrationTesting/mysql/expecting/mysqlxmlit/deletionOrder.txt");
    private static URL expectedInsertionOrder = MysqlXMLIT.class.getResource("/integrationTesting/mysql/expecting/mysqlxmlit/insertionOrder.txt");

    @ClassRule
    public static JdbcContainerRule<MySQLContainer> jdbcContainerRule =
            new SuiteOrTestJdbcContainerRule<>(
                    MysqlSuite.jdbcContainerRule,
                    new JdbcContainerRule<MySQLContainer>(() -> new MySQLContainer<>("mysql:5"))
                            .assumeDockerIsPresent().withAssumptions(assumeDriverIsPresent())
                            .withQueryString("?useSSL=false")
                            .withInitScript("integrationTesting/mysql/dbScripts/xmlit.sql")
                            .withInitUser("root", "test")
            );

    @BeforeClass
    public static void generateXML() throws Exception {
        MySQLContainer container = jdbcContainerRule.getContainer();
        String[] args = new String[] {
                "-t", "mysql",
                "-db", "xmlit",
                "-s", "xmlit",
                "-host", container.getContainerIpAddress() + ":" + String.valueOf(container.getMappedPort(3306)),
                "-port", String.valueOf(container.getMappedPort(3306)),
                "-u", container.getUsername(),
                "-p", container.getPassword(),
                "-nohtml",
                "-o", "target/mysqlxmlit",
                "-connprops","useSSL\\=false"
        };
        Main.main(args);
    }

    @Test
    public void verifyXML() {
        Diff d = DiffBuilder.compare(Input.fromURL(expectedXML))
                .withTest(Input.fromFile("target/mysqlxmlit/xmlit.xmlit.xml"))
                .withDifferenceEvaluator(DifferenceEvaluators.chain(DifferenceEvaluators.Default, new IgnoreUsingXPath("/database[1]/@type")))
                .build();
        assertThat(d.getDifferences()).isEmpty();
    }

    @Test
    public void verifyDeletionOrder() throws IOException {
        assertThat(Files.newInputStream(Paths.get("target/mysqlxmlit/deletionOrder.txt"), StandardOpenOption.READ)).hasSameContentAs(expectedDeletionOrder.openStream());
    }

    @Test
    public void verifyInsertionOrder() throws IOException {
        assertThat(Files.newInputStream(Paths.get("target/mysqlxmlit/insertionOrder.txt"), StandardOpenOption.READ)).hasSameContentAs(expectedInsertionOrder.openStream());
    }
}
