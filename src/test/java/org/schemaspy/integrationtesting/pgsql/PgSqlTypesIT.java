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
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.schemaspy.integrationtesting.PgSqlSuite;
import org.schemaspy.model.Database;
import org.schemaspy.model.Type;
import org.schemaspy.testing.SQLScriptsRunner;
import org.schemaspy.testing.SuiteOrTestJdbcContainerRule;
import org.testcontainers.containers.PostgreSQLContainer;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.schemaspy.testing.DatabaseFixture.database;

public class PgSqlTypesIT {

    private static final Path outputPath = Paths.get("target","testout","integrationtesting","pgsql","types");

    private static Database database;

    @SuppressWarnings("unchecked")
    @ClassRule
    public static JdbcContainerRule<PostgreSQLContainer<?>> jdbcContainerRule =
            new SuiteOrTestJdbcContainerRule<PostgreSQLContainer<?>>(
                    PgSqlSuite.jdbcContainerRule,
                    new JdbcContainerRule<PostgreSQLContainer<?>>(() -> new PostgreSQLContainer<>("postgres:10.4"))
                            .assumeDockerIsPresent()
                            .withAssumptions(assumeDriverIsPresent())
                            .withInitFunctions(new SQLScriptsRunner("integrationTesting/pgsql/dbScripts/types.sql", "\n\n\n"))
            );

    @Before
    public synchronized void createDatabaseRepresentation() throws SQLException, IOException {
        if (database == null) {
            doCreateDatabaseRepresentation();
        }
    }

    private void doCreateDatabaseRepresentation() throws SQLException, IOException {
        String[] args = {
            "-t", "pgsql",
            "-db", "test",
            "-s", "type_tests",
            "-cat", "%",
            "-o", outputPath.toString(),
            "-u", "test",
            "-p", "test",
            "-host", jdbcContainerRule.getContainer().getHost(),
            "-port", jdbcContainerRule.getContainer().getMappedPort(5432).toString()
        };
        database = database(args);
    }

    @Test
    public void databaseShouldExist() {
        assertThat(database).isNotNull();
        assertThat(database.getName()).isEqualToIgnoringCase("test");
    }

    @Test
    public void baseTypeIsDefined() {
        Type type = database.getTypesMap().get("type_tests.base_type");
        assertThat(type).isNotNull();
        assertThat(type.getTypeOfType()).isEqualTo("Base");
        assertThat(type.getSchema()).isEqualTo("type_tests");
        assertThat(type.getCatalog()).isNull();
        assertThat(type.getDescription()).isEqualTo("Description for base type_tests.base_type");
        assertThat(type.getDefinition()).contains("base_type_in");
        assertThat(type.getDefinition()).contains("base_type_out");
    }

    @Test
    public void compositeTypeIsDefined() {
        Type type = database.getTypesMap().get("type_tests.composite_type");
        assertThat(type).isNotNull();
        assertThat(type.getTypeOfType()).isEqualTo("Composite");
        assertThat(type.getSchema()).isEqualTo("type_tests");
        assertThat(type.getCatalog()).isNull();
        assertThat(type.getDescription()).isEqualTo("Description for composite type_tests.composite_type");
        assertThat(type.getDefinition()).contains("Description for column type_tests.composite_type.att1");
        assertThat(type.getDefinition()).contains("Description for column type_tests.composite_type.att2");
    }

    @Test
    public void domainIsDefined() {
        Type type = database.getTypesMap().get("type_tests.test_domain");
        assertThat(type).isNotNull();
        assertThat(type.getTypeOfType()).isEqualTo("Domain");
        assertThat(type.getSchema()).isEqualTo("type_tests");
        assertThat(type.getCatalog()).isNull();
        assertThat(type.getDescription()).isEqualTo("Description for domain type_tests.test_domain");
        assertThat(type.getDefinition()).isEqualTo("integer NOT NULL\n" +
                "DEFAULT 1\n" +
                "CHECK (VALUE < 4)");
    }

    @Test
    public void enumTypeIsDefined() {
        Type type = database.getTypesMap().get("type_tests.test_enum");
        assertThat(type).isNotNull();
        assertThat(type.getTypeOfType()).isEqualTo("Enum");
        assertThat(type.getSchema()).isEqualTo("type_tests");
        assertThat(type.getCatalog()).isNull();
        assertThat(type.getDescription()).isEqualTo("Description for enum type_tests.test_enum");
        assertThat(type.getDefinition()).isEqualTo("{a, b, c}");
    }

    @Test
    public void pseudoTypeIsDefined() {
        Type type = database.getTypesMap().get("type_tests.test_pseudo");
        assertThat(type).isNotNull();
        assertThat(type.getTypeOfType()).isEqualTo("Pseudo-type");
        assertThat(type.getSchema()).isEqualTo("type_tests");
        assertThat(type.getCatalog()).isNull();
        assertThat(type.getDescription()).isEqualTo("Description for pseudo type_tests.test_pseudo");
        assertThat(type.getDefinition()).isEqualTo("");
    }
}
