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
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.schemaspy.integrationtesting.MysqlSuite;
import org.schemaspy.model.Database;
import org.schemaspy.testing.SuiteOrTestJdbcContainerRule;
import org.testcontainers.containers.MySQLContainer;

import javax.script.ScriptException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.schemaspy.testing.DatabaseFixture.database;

public class MysqlSchemaLeakageIT {

    private static final Path outputPath = Paths.get("target","testout","integrationtesting","mysql","schema_leakage");

    private static Database database;

    @SuppressWarnings("unchecked")
    @ClassRule
    public static JdbcContainerRule<MySQLContainer<?>> jdbcContainerRule =
            new SuiteOrTestJdbcContainerRule<MySQLContainer<?>>(
                    MysqlSuite.jdbcContainerRule,
                    new JdbcContainerRule<MySQLContainer<?>>(() -> new MySQLContainer<>("mysql:8-oracle"))
                            .assumeDockerIsPresent()
                            .withAssumptions(assumeDriverIsPresent())
                            .withQueryString("?useSSL=false&allowPublicKeyRetrieval=true")
                            .withInitScript("integrationTesting/mysql/dbScripts/mysql_table_view_collision.sql")
                            .withInitUser("root", "test")
            );

    @Before
    public synchronized void createDatabaseRepresentation() throws SQLException, IOException, ScriptException, URISyntaxException {
        if (database == null) {
            doCreateDatabaseRepresentation();
        }
    }

    private void doCreateDatabaseRepresentation() throws SQLException, IOException {
        String[] args = {
                "-t", "mysql",
                "-db", "schemaleak",
                "-s", "schemaleak",
                "-cat", "%",
                "-u", "testUser",
                "-p", "password",
                "-host", jdbcContainerRule.getContainer().getHost(),
                "-port", jdbcContainerRule.getContainer().getMappedPort(3306).toString(),
                "-o", outputPath.toString(),
                "-connprops", "useSSL\\=false;allowPublicKeyRetrieval\\=true"
        };
        database = database(args);
    }

    @Test
    public void shouldHaveNoViews() {
        assertThat(database.getViews()).isEmpty();
    }
}
