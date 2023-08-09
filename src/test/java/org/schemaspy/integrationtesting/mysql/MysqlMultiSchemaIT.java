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
import org.schemaspy.integrationtesting.MysqlSuite;
import org.schemaspy.testing.HtmlOutputValidator;
import org.schemaspy.testing.SQLScriptsRunner;
import org.schemaspy.testing.SuiteOrTestJdbcContainerRule;
import org.testcontainers.containers.MySQLContainer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.schemaspy.testing.SchemaSpyRunnerFixture.schemaSpyRunner;

/**
 * @author Nils Petzaell
 */
public class MysqlMultiSchemaIT {

    private static final Path outputPath = Paths.get("target","testout","integrationtesting","mysql","multischema");

    @SuppressWarnings("unchecked")
    @ClassRule
    public static JdbcContainerRule<MySQLContainer<?>> jdbcContainerRule =
            new SuiteOrTestJdbcContainerRule<MySQLContainer<?>>(
                    MysqlSuite.jdbcContainerRule,
                    new JdbcContainerRule<MySQLContainer<?>>(() -> new MySQLContainer<>("mysql:8-oracle"))
                            .assumeDockerIsPresent()
                            .withAssumptions(assumeDriverIsPresent())
                            .withQueryString("?useSSL=false&allowPublicKeyRetrieval=true")
                            .withInitFunctions(new SQLScriptsRunner("integrationTesting/mysql/dbScripts/"))
                            .withInitUser("root", "test")
            );

    private static final AtomicBoolean shouldRun = new AtomicBoolean(true);

    @Before
    public synchronized void generateHTML() {
        if (shouldRun.get()) {
            String[] args = new String[]{
                    "-t", "mysql",
                    "-db", "htmlit",
                    "-all",
                    "-schemaSpec", "(?!^mysql$|^performance_schema$|^information_schema$).*",
                    "-host", jdbcContainerRule.getContainer().getHost() + ":" + jdbcContainerRule.getContainer().getMappedPort(3306),
                    "-port", String.valueOf(jdbcContainerRule.getContainer().getMappedPort(3306)),
                    "-u", jdbcContainerRule.getContainer().getUsername(),
                    "-p", jdbcContainerRule.getContainer().getPassword(),
                    "-o", outputPath.toString(),
                    "-connprops", "useSSL\\=false;allowPublicKeyRetrieval\\=true",
                    "--no-orphans"
            };
            schemaSpyRunner(args).run();
            shouldRun.set(false);
        }
    }

    @Test
    public void producesSameContentForIndex() throws IOException {
        SoftAssertions softAssertions = HtmlOutputValidator
                .hasSameContent(
                        outputPath.resolve("index.html"),
                        Paths.get("src", "test", "resources", "integrationTesting", "mysql", "expecting", "mysqlmultischema", "index.html")
                );
        softAssertions.assertThat(softAssertions.wasSuccess()).isTrue();
        softAssertions.assertAll();
    }

    @Test
    public void producesSameContentForSchema() throws IOException {
        SoftAssertions softAssertions = HtmlOutputValidator
                .hasProducedValidOutput(
                        outputPath.resolve("htmlit"),
                        Paths.get("src", "test", "resources", "integrationTesting", "mysql", "expecting", "mysqlmultischema", "htmlit")
                );
        softAssertions.assertThat(softAssertions.wasSuccess()).isTrue();
        softAssertions.assertAll();
    }
}
