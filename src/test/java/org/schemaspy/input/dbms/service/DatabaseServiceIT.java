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

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.schemaspy.cli.CommandLineArgumentParser;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.model.Sequence;
import org.schemaspy.testing.H2MemoryRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Nils Petzaell
 * @author Thomas Traude
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DatabaseServiceIT {

    private static String CREATE_SCHEMA = "CREATE SCHEMA DATABASESERVICEIT AUTHORIZATION SA";
    private static String SET_SCHEMA = "SET SCHEMA DATABASESERVICEIT";
    private static String CREATE_TABLE = "CREATE TABLE TEST(ID INT PRIMARY KEY, NAME VARCHAR(255))";
    private static String CREATE_SEQUENCE = "CREATE SEQUENCE SEQ_CLIENT start with 5 increment by 2";

    @Rule
    public H2MemoryRule h2MemoryRule = new H2MemoryRule("DatabaseServiceIT").addSqls(
        CREATE_SCHEMA,
        SET_SCHEMA,
        CREATE_TABLE,
        CREATE_SEQUENCE
    );

    @Autowired
    private SqlService sqlService;

    @Mock
    private ProgressListener progressListener;

    @Test
    public void gatheringSchemaDetailsTest() throws Exception {
        String[] args = {
            "-t", "src/test/resources/integrationTesting/dbTypes/h2memory",
            "-db", "DatabaseServiceIT",
            "-s", h2MemoryRule.getConnection().getSchema(),
            "-cat", h2MemoryRule.getConnection().getCatalog(),
            "-o", "target/integrationtesting/databaseServiceIT",
            "-u", "sa"
        };

        CommandLineArguments arguments = new CommandLineArgumentParser(
            new CommandLineArguments(),
            (option) -> null
        ).parse(args);
        sqlService.connect(arguments.getConnectionConfig());
        Database database = new Database(
            sqlService.getDbmsMeta(),
            arguments.getConnectionConfig().getDatabaseName(),
            arguments.getCatalog(),
            arguments.getSchema()
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(arguments.getProcessingConfig())
                                              .gatherSchemaDetails(database, null, progressListener);

        assertThat(database.getTables()).hasSize(1);

        //check sequence
        Collection<Sequence> sequences = database.getSequences();
        assertThat(sequences.stream().map(seq -> seq.getName())).containsExactlyInAnyOrder("SEQ_CLIENT");
        assertThat(sequences.iterator().next().getStartValue()).isEqualTo(5);
        assertThat(sequences.iterator().next().getIncrement()).isEqualTo(2);
    }
}
