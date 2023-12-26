/*
 * Copyright (C) 2017, 2018 Nils Petzaell
 * Copyright (C) 2017 Thomas Traude
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
package org.schemaspy.input.dbms.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.schemaspy.model.Database;
import org.schemaspy.model.Sequence;
import org.schemaspy.testing.H2MemoryExtension;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.schemaspy.testing.DatabaseFixture.database;

/**
 * @author Nils Petzaell
 * @author Thomas Traude
 */
class DatabaseServiceIT {

    private static final String CREATE_SCHEMA = "CREATE SCHEMA DATABASESERVICEIT AUTHORIZATION SA";
    private static final String SET_SCHEMA = "SET SCHEMA DATABASESERVICEIT";
    private static final String CREATE_TABLE = "CREATE TABLE TEST(ID INT PRIMARY KEY, NAME VARCHAR(255))";
    private static final String CREATE_SEQUENCE = "CREATE SEQUENCE SEQ_CLIENT start with 5 increment by 2";

    @RegisterExtension
    static H2MemoryExtension h2 = new H2MemoryExtension("DatabaseServiceIT")
            .addSqls(
                    CREATE_SCHEMA,
                    SET_SCHEMA,
                    CREATE_TABLE,
                    CREATE_SEQUENCE
            );

    @Test
    void gatheringSchemaDetailsTest() throws Exception {
        String[] args = {
            "-t", "src/test/resources/integrationTesting/dbTypes/h2memory",
            "-db", "DatabaseServiceIT",
            "-s", h2.getConnection().getSchema(),
            "-cat", h2.getConnection().getCatalog(),
            "-o", "target/integrationtesting/databaseServiceIT",
            "-u", "sa"
        };
        Database database = database(args);

        assertThat(database.getTables()).hasSize(1);

        //check sequence
        Collection<Sequence> sequences = database.getSequences();
        assertThat(sequences.stream().map(Sequence::getName)).containsExactlyInAnyOrder("SEQ_CLIENT");
        assertThat(sequences.iterator().next().getStartValue()).isEqualTo(5);
        assertThat(sequences.iterator().next().getIncrement()).isEqualTo(2);
    }
}
