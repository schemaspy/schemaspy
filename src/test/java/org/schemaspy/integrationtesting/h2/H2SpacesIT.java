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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.schemaspy.model.Database;
import org.schemaspy.model.Table;
import org.schemaspy.testing.H2MemoryExtension;

import javax.script.ScriptException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.schemaspy.testing.DatabaseFixture.database;

/**
 * @author Nils Petzaell
 */
class H2SpacesIT {

    @RegisterExtension
    static H2MemoryExtension h2 = new H2MemoryExtension("h2 spaces")
            .addSqlScript("src/test/resources/integrationTesting/h2/dbScripts/spaces_in_schema_and_table.sql");

    private static Database database;

    @BeforeAll
    static void createDatabaseRepresentation() throws SQLException, IOException, ScriptException, URISyntaxException {
        String[] args = {
                "-t", "src/test/resources/integrationTesting/dbTypes/h2memory",
                "-db", "h2 spaces",
                "-s", h2.getConnection().getSchema(),
                "-cat", h2.getConnection().getCatalog(),
                "-o", "target/testout/integrationtesting/h2/ spaces",
                "-u", "sa"
        };
        database = database(args);
    }

    @Test
    void databaseShouldExist() {
        assertThat(database).isNotNull();
        assertThat(database.getName()).isEqualToIgnoringCase("h2 spaces");
    }

    @Test
    void tableWithSpacesShouldExist() {
        assertThat(database.getTables()).extracting(Table::getName).contains("has space");
    }
}
