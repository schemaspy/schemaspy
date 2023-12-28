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
package org.schemaspy.integrationtesting.informix;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.schemaspy.integrationtesting.InformixSuite;
import org.schemaspy.model.Database;
import org.schemaspy.model.Table;
import org.schemaspy.testing.SuiteContainerExtension;

import java.io.IOException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.schemaspy.testing.DatabaseFixture.database;

/**
 * @author Nils Petzaell
 */
@DisabledOnOs(value = OS.MAC, architectures = {"aarch64"})
class InformixCheckConstraintIT {

    private static Database database;

    @RegisterExtension
    static SuiteContainerExtension container = InformixSuite.SUITE_CONTAINER;

    @BeforeAll
    static void gatheringSchemaDetailsTest() throws SQLException, IOException {
        String[] args = {
                "-t", "informix",
                "-db", "testconstraint",
                "-s", "informix",
                "-cat", "testconstraint",
                "-server", "dev",
                "-o", "target/testout/integrationtesting/informix/cc",
                "-u", container.getUsername(),
                "-p", container.getPassword(),
                "-host", container.getHost(),
                "-port", container.getPort(9088)
        };
        database = database(args);
    }

    @Test
    void databaseShouldBePopulatedWithTableTest() {
        Table table = getTable("test");
        assertThat(table).isNotNull();
    }

    @Test
    void tableTesShouldContainCheckConstraint() {
        String expecting = "((((((LENGTH (firstname ) > 10 ) AND (LENGTH (lastname ) > 10 ) ) AND ((age >= 100 ) AND (age <= 105 ) ) ) AND ((weight >= 100 ) AND (weight <= 105 ) ) ) AND ((height >= 100 ) AND (height <= 105 ) ) ) OR (((((LENGTH (firstname ) > 13 ) AND (LENGTH (lastname ) > 13 ) ) AND ((age >= 106 ) AND (age <= 108 ) ) ) AND ((weight >= 106 ) AND (weight <= 108 ) ) ) AND ((height >= 106 ) AND (height <= 108 ) ) ) )";
        Table table = getTable("test");
        String actual = table.getCheckConstraints().get("big_check").trim();
        assertThat(actual).isEqualToIgnoringCase(expecting);
    }

    private Table getTable(String tableName) {
        return database.getTablesMap().get(tableName);
    }
}
