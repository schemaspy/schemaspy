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
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.schemaspy.Config;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.input.dbms.service.DatabaseServiceFactory;
import org.schemaspy.input.dbms.service.SqlService;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.model.Table;
import org.schemaspy.testing.H2MemoryRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * @author Nils Petzaell
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class H2SpacesIT {

    @ClassRule
    public static H2MemoryRule h2MemoryRule = new H2MemoryRule("h2 spaces").addSqlScript("src/test/resources/integrationTesting/h2/dbScripts/spaces_in_schema_and_table.sql");

    @Autowired
    private SqlService sqlService;

    @Mock
    private ProgressListener progressListener;

    @MockBean
    private CommandLineArguments arguments;

    @MockBean
    private CommandLineRunner commandLineRunner;

    private static Database database;

    @Before
    public synchronized void createDatabaseRepresentation() throws SQLException, IOException, ScriptException, URISyntaxException {
        if (database == null) {
            doCreateDatabaseRepresentation();
        }
    }

    private void doCreateDatabaseRepresentation() throws SQLException, IOException {
        String[] args = {
                "-t", "src/test/resources/integrationTesting/dbTypes/h2memory",
                "-db", "h2 spaces",
                "-s", "h2 spaces",
                "-o", "target/testout/integrationtesting/h2/ spaces",
                "-u", "sa"
        };
        given(arguments.getOutputDirectory()).willReturn(new File("target/testout/integrationtesting/h2/ spaces"));
        given(arguments.getDatabaseType()).willReturn("src/test/resources/integrationTesting/dbTypes/h2memory");
        given(arguments.getUser()).willReturn("sa");
        given(arguments.getCatalog()).willReturn(h2MemoryRule.getConnection().getCatalog());
        given(arguments.getSchema()).willReturn(h2MemoryRule.getConnection().getSchema());
        given(arguments.getDatabaseName()).willReturn("h2 spaces");
        Config config = new Config(args);
        sqlService.connect(config);
        Database database = new Database(
                sqlService.getDbmsMeta(),
                arguments.getDatabaseName(),
                arguments.getCatalog(),
                arguments.getSchema()
        );
        new DatabaseServiceFactory(sqlService).simple(config).gatherSchemaDetails(database, null, progressListener);
        H2SpacesIT.database = database;
    }

    @Test
    public void databaseShouldExist() {
        assertThat(database).isNotNull();
        assertThat(database.getName()).isEqualToIgnoringCase("h2 spaces");
    }

    @Test
    public void tableWithSpacesShouldExist() {
        assertThat(database.getTables()).extracting(Table::getName).contains("has space");
    }
}
