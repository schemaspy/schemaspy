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
package org.schemaspy.integrationtesting.pgsql;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.schemaspy.cli.SchemaSpyRunner;
import org.schemaspy.integrationtesting.PgSqlSuite;
import org.schemaspy.testing.SQLScriptsRunner;
import org.schemaspy.testing.SuiteOrTestJdbcContainerRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.PostgreSQLContainer;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;

/**
 * @author Nils Petzaell
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
@Ignore
public class PgSqlCrossGvPNG {

    private Path outputPath = Paths.get("target","testout","integrationtesting","pgsql", "cross_gv_png");

    private static URL expectedXML = PgSqlCrossGvPNG.class.getResource("/integrationTesting/pgsql/expecting/cross_gv_png/htmlit.htmlit.xml");
    private static URL expectedDeletionOrder = PgSqlCrossGvPNG.class.getResource("/integrationTesting/pgsql/expecting/cross_gv_png/deletionOrder.txt");
    private static URL expectedInsertionOrder = PgSqlCrossGvPNG.class.getResource("/integrationTesting/pgsql/expecting/cross_gv_png/insertionOrder.txt");

    @SuppressWarnings("unchecked")
    @ClassRule
    public static JdbcContainerRule<PostgreSQLContainer<?>> jdbcContainerRule =
            new SuiteOrTestJdbcContainerRule<PostgreSQLContainer<?>>(
                    PgSqlSuite.jdbcContainerRule,
                    new JdbcContainerRule<PostgreSQLContainer<?>>(() -> new PostgreSQLContainer<>("postgres:10.4"))
                            .assumeDockerIsPresent()
                            .withAssumptions(assumeDriverIsPresent())
                            .withInitFunctions(new SQLScriptsRunner("integrationTesting/pgsql/dbScripts/cross_schema_fk.sql", "\n\n\n"))
            );

    @Autowired
    private SchemaSpyRunner schemaSpyRunner;

    private static final AtomicBoolean shouldRun = new AtomicBoolean(true);

    @Before
    public synchronized void generateHTML() throws Exception {
        if (shouldRun.get()) {
            String[] args = {
                    "-t", "pgsql",
                    "-db", "test",
                    "-all",
                    "-schemaSpec", "sch(a|b)",
                    "-cat", "%",
                    "-o", outputPath.toString(),
                    "-u", "test",
                    "-p", "test",
                    "-host", jdbcContainerRule.getContainer().getContainerIpAddress(),
                    "-port", jdbcContainerRule.getContainer().getMappedPort(5432).toString()
            };
            schemaSpyRunner.run(args);
            shouldRun.set(false);
        }
    }

    @Test
    public void verifyDiagramContent(){

    }
}
