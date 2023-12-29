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

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.schemaspy.integrationtesting.MysqlSuite;
import org.schemaspy.testing.HtmlOutputValidator;
import org.schemaspy.testing.testcontainers.SuiteContainerExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.schemaspy.testing.SchemaSpyRunnerFixture.schemaSpyRunner;

/**
 * @author Nils Petzaell
 */
class MysqlMultiSchemaIT {

    private static final Path outputPath =
            Paths.get("target","testout","integrationtesting","mysql","multischema");

    @RegisterExtension
    static SuiteContainerExtension container = MysqlSuite.SUITE_CONTAINER;

    @BeforeAll
    static void generateHTML() {
        String[] args = new String[]{
                "-t", "mysql",
                "-db", "htmlit",
                "-all",
                "-schemaSpec", "(?!^mysql$|^performance_schema$|^information_schema$).*",
                "-host", container.getHost() + ":" + container.getPort(3306),
                "-port", container.getPort(3306),
                "-u", container.getUsername(),
                "-p", container.getPassword(),
                "-o", outputPath.toString(),
                "-connprops", "useSSL\\=false;allowPublicKeyRetrieval\\=true",
                "--no-orphans",
                "--include-routine-definition"
        };
        schemaSpyRunner(args).run();
    }

    @Test
    void producesSameContentForIndex() throws IOException {
        SoftAssertions softAssertions = HtmlOutputValidator
                .hasSameContent(
                        outputPath.resolve("index.html"),
                        Paths.get("src", "test", "resources", "integrationTesting", "mysql", "expecting", "mysqlmultischema", "index.html")
                );
        softAssertions.assertThat(softAssertions.wasSuccess()).isTrue();
        softAssertions.assertAll();
    }

    @Test
    void producesSameContentForSchema() throws IOException {
        SoftAssertions softAssertions = HtmlOutputValidator
                .hasProducedValidOutput(
                        outputPath.resolve("htmlit"),
                        Paths.get("src", "test", "resources", "integrationTesting", "mysql", "expecting", "mysqlmultischema", "htmlit")
                );
        softAssertions.assertThat(softAssertions.wasSuccess()).isTrue();
        softAssertions.assertAll();
    }
}
