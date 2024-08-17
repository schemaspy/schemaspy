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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.schemaspy.integrationtesting.MysqlSuite;
import org.schemaspy.model.Database;
import org.schemaspy.testing.testcontainers.SuiteContainerExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.schemaspy.testing.DatabaseFixture.database;

class MysqlSchemaLeakageIT {

    private static final Path outputPath = Paths.get("target","testout","integrationtesting","mysql","schema_leakage");

    private static Database database;

    @RegisterExtension
    static SuiteContainerExtension container = MysqlSuite.SUITE_CONTAINER;

    @BeforeAll
    static void createDatabaseRepresentation() throws SQLException, IOException {
        String[] args = {
                "-t", "mysql",
                "-db", "schemaleak",
                "-s", "schemaleak",
                "-cat", "%",
                "-u", "testUser",
                "-p", "password",
                "-host", container.getHost(),
                "-port", container.getPort(3306),
                "-o", outputPath.toString(),
                "-connprops", "useSSL\\=false;allowPublicKeyRetrieval\\=true"
        };
        database = database(args);
    }

    @Test
    void shouldHaveNoViews() {
        assertThat(database.getViews()).isEmpty();
    }
}
