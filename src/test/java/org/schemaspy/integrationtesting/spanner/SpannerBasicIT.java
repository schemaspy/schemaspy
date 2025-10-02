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
package org.schemaspy.integrationtesting.spanner;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.schemaspy.integrationtesting.SpannerSuite;
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
 * Basic integration test for Google Cloud Spanner support.
 * 
 * Tests fundamental functionality of SchemaSpy with Cloud Spanner:
 * - Connection to Spanner emulator
 * - Table detection and metadata extraction
 * - Column information retrieval
 * - Primary key detection
 * - Foreign key relationship verification
 * 
 * Unlike BigQuery, Spanner is a relational database with full support for:
 * - Primary keys (required for all tables)
 * - Foreign keys (referential integrity)
 * - Indexes (secondary indexes for query optimization)
 */
class SpannerBasicIT {

    private static final Path outputPath = Paths.get("target", "testout", "integrationtesting", "spanner", "basic");

    private static Database database;

    @RegisterExtension
    static SuiteContainerExtension container = SpannerSuite.SUITE_CONTAINER;

    @BeforeAll
    static void createDatabaseRepresentation() throws SQLException, IOException {
        String[] args = {
                "-t", "spanner",
                "-db", "test-database",
                "-s", "", // Spanner uses empty or database name as schema
                "-cat", "test-project",
                "-host", container.getHost(),
                "-port", container.getPort(9010), // Spanner emulator gRPC port
                "-o", outputPath.toString(),
                "-u", "test",
                "-connprops", "instance\\;test-instance;autoConfigEmulator\\;true"
        };
        database = database(args);
    }

    @Test
    void databaseShouldBeInitialized() {
        assertThat(database).isNotNull();
        assertThat(database.getName()).isEqualTo("test-database");
    }

    @Test
    void shouldHaveCustomersTable() {
        assertThat(database.getTables())
                .extracting(Table::getName)
                .contains("Customers");
    }

    @Test
    void shouldHaveOrdersTable() {
        assertThat(database.getTables())
                .extracting(Table::getName)
                .contains("Orders");
    }

    @Test
    void customersTableShouldHaveExpectedColumns() {
        Table customersTable = database.getTablesMap().get("Customers");
        assertThat(customersTable).isNotNull();
        assertThat(customersTable.getColumns())
                .extracting("name")
                .containsExactlyInAnyOrder("CustomerId", "CustomerName", "Email", "CreatedAt");
    }

    @Test
    void ordersTableShouldHaveExpectedColumns() {
        Table ordersTable = database.getTablesMap().get("Orders");
        assertThat(ordersTable).isNotNull();
        assertThat(ordersTable.getColumns())
                .extracting("name")
                .containsExactlyInAnyOrder("OrderId", "CustomerId", "OrderDate", "TotalAmount");
    }

    @Test
    void customersTableShouldHavePrimaryKey() {
        Table customersTable = database.getTablesMap().get("Customers");
        assertThat(customersTable).isNotNull();
        assertThat(customersTable.getPrimaryColumns())
                .hasSize(1)
                .extracting("name")
                .contains("CustomerId");
    }

    @Test
    void ordersTableShouldHaveForeignKey() {
        Table ordersTable = database.getTablesMap().get("Orders");
        assertThat(ordersTable).isNotNull();
        // Check if there's a foreign key relationship to Customers
        assertThat(ordersTable.getForeignKeys()).isNotEmpty();
    }

    @Test
    void shouldHaveCorrectNumberOfTables() {
        assertThat(database.getTables()).hasSize(2);
    }
}
