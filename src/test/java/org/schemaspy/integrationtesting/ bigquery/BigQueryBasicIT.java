/*
 * Copyright (C) 2025 SchemaSpy Contributors
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
package org.schemaspy.integrationtesting.bigquery;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.schemaspy.integrationtesting.BigQuerySuite;
import org.schemaspy.model.Database;
import org.schemaspy.model.Table;
import org.schemaspy.testing.testcontainers.SuiteContainerExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.schemaspy.testing.DatabaseFixture.database;

/**
 * Basic integration test for BigQuery support.
 * 
 * Tests fundamental functionality of SchemaSpy with BigQuery:
 * - Connection to BigQuery emulator
 * - Table detection and metadata extraction
 * - Column information retrieval
 */
class BigQueryBasicIT {

    private static final Path outputPath = Paths.get("target", "testout", "integrationtesting", "bigquery", "basic");

    private static Database database;

    @RegisterExtension
    static SuiteContainerExtension container = BigQuerySuite.SUITE_CONTAINER;

    @BeforeAll
    static void createDatabaseRepresentation() throws SQLException, IOException {
        String[] args = {
                "-t", "bigquery",
                "-db", "test_dataset",
                "-s", "test_dataset",
                "-cat", "test-project",
                "-host", container.getHost(),
                "-port", container.getPort(9050),
                "-o", outputPath.toString(),
                "-u", "test"
        };
        database = database(args);
    }

    @Test
    void databaseShouldBeInitialized() {
        assertThat(database).isNotNull();
        assertThat(database.getName()).isEqualTo("test_dataset");
    }

    @Test
    void shouldHaveCustomersTable() {
        assertThat(database.getTables())
                .extracting(Table::getName)
                .contains("customers");
    }

    @Test
    void shouldHaveOrdersTable() {
        assertThat(database.getTables())
                .extracting(Table::getName)
                .contains("orders");
    }

    @Test
    void customersTableShouldHaveExpectedColumns() {
        Table customersTable = database.getTablesMap().get("customers");
        assertThat(customersTable).isNotNull();
        assertThat(customersTable.getColumns())
                .extracting("name")
                .containsExactlyInAnyOrder("id", "name", "email", "created_at");
    }

    @Test
    void ordersTableShouldHaveExpectedColumns() {
        Table ordersTable = database.getTablesMap().get("orders");
        assertThat(ordersTable).isNotNull();
        assertThat(ordersTable.getColumns())
                .extracting("name")
                .containsExactlyInAnyOrder("order_id", "customer_id", "order_date", "total_amount");
    }

    @Test
    void shouldHaveCorrectNumberOfTables() {
        assertThat(database.getTables()).hasSize(2);
    }

}
