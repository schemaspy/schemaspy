/*
 * Copyright (C) 2017, 2018 Nils Petzaell
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
package org.schemaspy.integrationtesting.h2;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.schemaspy.model.Database;
import org.schemaspy.model.Table;
import org.schemaspy.testing.H2MemoryRule;

import java.io.IOException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.schemaspy.testing.DatabaseFixture.database;

/**
 * @author Nils Petzaell
 */
public class H2ViewIT {

    @ClassRule
    public static H2MemoryRule h2MemoryRule = new H2MemoryRule("h2view").addSqlScript("src/test/resources/integrationTesting/h2/dbScripts/2tables1view.sql");

    private static Database database;

    @Before
    public synchronized void createDatabaseRepresentation() throws SQLException, IOException {
        if (database == null) {
            doCreateDatabaseRepresentation();
        }
    }

    private void doCreateDatabaseRepresentation() throws SQLException, IOException {
        String[] args = {
            "-t", "src/test/resources/integrationTesting/dbTypes/h2memory",
            "-db", "h2view",
            "-s", h2MemoryRule.getConnection().getSchema(),
            "-cat", h2MemoryRule.getConnection().getCatalog(),
            "-o", "target/testout/integrationtesting/h2/view",
            "-u", "sa"
        };
        database = database(args);
    }

    @Test
    public void databaseShouldExist() {
        assertThat(database).isNotNull();
        assertThat(database.getName()).isEqualToIgnoringCase("h2view");
    }

    @Test
    public void viewShouldExist() {
        assertThat(database.getViews()).extracting(Table::getName).contains("THE_VIEW");
        assertThat(database.getViewsMap().get("THE_VIEW").getViewDefinition()).isNotBlank();
    }
}
