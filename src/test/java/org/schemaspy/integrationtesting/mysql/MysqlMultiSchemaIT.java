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
import org.schemaspy.testing.SQLScriptsRunner;
import org.schemaspy.testing.SuiteOrTestJdbcContainerRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.MySQLContainer;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;

/**
 * @author Nils Petzaell
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class MysqlMultiSchemaIT {

    @ClassRule
    public static JdbcContainerRule<MySQLContainer> jdbcContainerRule =
            new SuiteOrTestJdbcContainerRule<>(
                    MysqlSuite.jdbcContainerRule,
                    new JdbcContainerRule<>(() -> new MySQLContainer("mysql:5"))
                            .assumeDockerIsPresent()
                            .withAssumptions(assumeDriverIsPresent())
                            .withQueryString("?useSSL=false")
                            .withInitFunctions(new SQLScriptsRunner("integrationTesting/mysql/dbScripts/"))
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
                    "-db", "htmlit",
                    "-all",
                    "-schemaSpec", "(?!^mysql$|^performance_schema$|^information_schema$).*",
                    "-host", jdbcContainerRule.getContainer().getContainerIpAddress() + ":" + jdbcContainerRule.getContainer().getMappedPort(3306),
                    "-port", String.valueOf(jdbcContainerRule.getContainer().getMappedPort(3306)),
                    "-u", jdbcContainerRule.getContainer().getUsername(),
                    "-p", jdbcContainerRule.getContainer().getPassword(),
                    "-o", "target/mysqlmultischema",
                    "-connprops", "useSSL\\=false"
            };
            schemaSpyRunner.run(args);
            shouldRun.set(false);
        }
    }

    @Test
    public void producesSameContentForIndex() throws IOException {
        SoftAssertions softAssertions = HtmlOutputValidator
                .hasSameContent(
                        Paths.get("target", "mysqlmultischema","index.html"),
                        Paths.get("src", "test", "resources", "integrationTesting", "mysql", "expecting", "mysqlmultischema", "index.html")
                );
        softAssertions.assertThat(softAssertions.wasSuccess()).isTrue();
        softAssertions.assertAll();
    }

    @Test
    public void producesSameContentForSchema() throws IOException {
        SoftAssertions softAssertions = HtmlOutputValidator
                .hasProducedValidOutput(
                        Paths.get("target", "mysqlmultischema","htmlit"),
                        Paths.get("src", "test", "resources", "integrationTesting", "mysql", "expecting", "mysqlmultischema", "htmlit")
                );
        softAssertions.assertThat(softAssertions.wasSuccess()).isTrue();
        softAssertions.assertAll();
    }
}
