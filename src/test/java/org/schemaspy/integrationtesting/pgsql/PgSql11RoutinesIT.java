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
import org.junit.Test;
import org.schemaspy.integrationtesting.PgSql11Suite;
import org.schemaspy.model.Database;
import org.schemaspy.testing.SQLScriptsRunner;
import org.schemaspy.testing.SuiteOrTestJdbcContainerRule;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.schemaspy.testing.DatabaseFixture.database;

public class PgSql11RoutinesIT {

    private static final Path outputPath = Paths.get("target","testout","integrationtesting","pgsql11","routines");

    private static Database database;

    @SuppressWarnings("unchecked")
    @ClassRule
    public static JdbcContainerRule<PostgreSQLContainer<?>> jdbcContainerRule =
            new SuiteOrTestJdbcContainerRule<PostgreSQLContainer<?>>(
                    PgSql11Suite.jdbcContainerRule,
                    new JdbcContainerRule<PostgreSQLContainer<?>>(() -> new PostgreSQLContainer<>("postgres:15.3"))
                            .assumeDockerIsPresent()
                            .withAssumptions(assumeDriverIsPresent())
                            .withInitFunctions(new SQLScriptsRunner("integrationTesting/pgsql/dbScripts/dvdrental.sql", "\n\n\n"))
            );

    @Before
    public synchronized void createDatabaseRepresentation() throws SQLException, IOException {
        if (database == null) {
            doCreateDatabaseRepresentation();
        }
    }

    private void doCreateDatabaseRepresentation() throws SQLException, IOException {
        String[] args = {
                "-t", "pgsql11",
                "-db", "test",
                "-s", "public",
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
    public void databaseShouldHave8Routines() {
        assertThat(database.getRoutines()).hasSize(10);
    }

    @Test
    public void routineFilmInStockHasComment() {
        assertThat(database.getRoutinesMap().get("film_in_stock(p_film_id integer, p_store_id integer, out p_film_count integer)").getComment()).isEqualToIgnoringCase("Current stock");
    }

    @Test
    public void routineFilmInStockHas3Parameters() {
        assertThat(database.getRoutinesMap().get("film_in_stock(p_film_id integer, p_store_id integer, out p_film_count integer)").getParameters()).hasSize(3);
    }
}
