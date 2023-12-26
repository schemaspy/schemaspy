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

import java.io.IOException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.schemaspy.testing.DatabaseFixture.database;

/**
 * @author Nils Petzaell
 */
class H2KeywordIT {

    @RegisterExtension
    static H2MemoryExtension h2 = new H2MemoryExtension("h2keyword")
            .addSqlScript("src/test/resources/integrationTesting/h2/dbScripts/keyword_in_table.sql");

    private static Database database;

    @BeforeAll
    static void createDatabaseRepresentation() throws SQLException, IOException {
        String[] args = {
                "-t", "src/test/resources/integrationTesting/dbTypes/h2memory",
                "-db", "h2keyword",
                "-s", h2.getConnection().getSchema(),
                "-o", "target/testout/integrationtesting/h2/keyword",
                "-u", "sa",
                "-cat", h2.getConnection().getCatalog(),

        };
        database = database(args);
    }

    @Test
    void databaseShouldExist() {
        assertThat(database).isNotNull();
        assertThat(database.getName()).isEqualToIgnoringCase("h2keyword");
    }

    @Test
    void tableWithKeyWordShouldExist() {
        assertThat(database.getTables()).extracting(Table::getName).contains("DISTINCT");
    }
}
