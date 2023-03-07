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
package org.schemaspy.integrationtesting;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.schemaspy.Config;
import org.schemaspy.SimpleDotConfig;
import org.schemaspy.analyzer.ImpliedConstraintsFinder;
import org.schemaspy.cli.CommandLineArgumentParser;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.input.dbms.service.DatabaseServiceFactory;
import org.schemaspy.input.dbms.service.SqlService;
import org.schemaspy.input.dbms.xml.SchemaMeta;
import org.schemaspy.model.Database;
import org.schemaspy.model.DbmsMeta;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.output.dot.schemaspy.DefaultFontConfig;
import org.schemaspy.output.dot.schemaspy.DotFormatter;
import org.schemaspy.testing.H2MemoryRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.concurrent.atomic.LongAdder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Nils Petzaell
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class SchemaMetaIT {

    private static String BY_SCRIPT_COMMENT = "Set by script";
    private static String BY_SCHEMA_META_COMMENT = "Set from SchemaMeta";

    @ClassRule
    public static H2MemoryRule h2MemoryRule = new H2MemoryRule("SchemaMetaIT").addSqlScript("src/test/resources/integrationTesting/schemaMetaIT/dbScripts/shemaMetaIT.h2.sql");

    @Autowired
    private SqlService sqlService;

    @Mock
    private ProgressListener progressListener;

    private Config config;
    private DbmsMeta dbmsMeta;
    private String schema;
    private String catalog;

    @Before
    public void setup() throws IOException, SQLException {
        String[] args = {
                "-t", "src/test/resources/integrationTesting/dbTypes/h2memory",
                "-db", "SchemaMetaIT",
                "-s", "SCHEMAMETAIT",
                "-o", "target/integrationtesting/schemaMetaIT",
                "-u", "sa"
        };
        config = new Config(args);
        CommandLineArguments arguments = new CommandLineArgumentParser(
            new CommandLineArguments(),
            (option) -> null
        ).parse(args);
        sqlService.connect(arguments,config);
        dbmsMeta = sqlService.getDbmsMeta();
        schema = h2MemoryRule.getConnection().getSchema();
        catalog = h2MemoryRule.getConnection().getCatalog();
    }

    @Test
    public void commentsNullTableComment() throws Exception {
        Database database = new Database(
                dbmsMeta,
                "DatabaseServiceIT",
                catalog,
                schema
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(config).gatherSchemaDetails(database, null, progressListener);

        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/integrationTesting/schemaMetaIT/input/nullTableComment.xml","SchemaMetaIT", schema, false);
        Database databaseWithSchemaMeta = new Database(
                dbmsMeta,
                "SchemaMetaIT",
                catalog,
                schema
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(config).gatherSchemaDetails(databaseWithSchemaMeta, schemaMeta, progressListener);

        assertThat(database.getTables()).isNotEmpty();
        assertThat(database.getSchema().getComment()).isEqualToIgnoringCase(BY_SCRIPT_COMMENT);
        assertThat(database.getTablesMap().get("ACCOUNT").getComments()).isEqualToIgnoringCase(BY_SCRIPT_COMMENT);
        assertThat(database.getTablesMap().get("ACCOUNT").getColumn("name").getComments()).isEqualToIgnoringCase(BY_SCRIPT_COMMENT);

        assertThat(databaseWithSchemaMeta.getTables()).isNotEmpty();
        assertThat(databaseWithSchemaMeta.getSchema().getComment()).isEqualToIgnoringCase(BY_SCRIPT_COMMENT);
        assertThat(databaseWithSchemaMeta.getTablesMap().get("ACCOUNT").getComments()).isEqualToIgnoringCase(BY_SCRIPT_COMMENT);
        assertThat(databaseWithSchemaMeta.getTablesMap().get("ACCOUNT").getColumn("name").getComments()).isEqualToIgnoringCase(BY_SCRIPT_COMMENT);
        assertThat(databaseWithSchemaMeta.getTablesMap().get("ACCOUNT").getColumn("accountId").getComments()).isEqualToIgnoringCase(BY_SCHEMA_META_COMMENT);
    }

    @Test
    public void commentsNoTableComment() throws SQLException {
        Database database = new Database(
                dbmsMeta,
                "DatabaseServiceIT",
                catalog,
                schema
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(config).gatherSchemaDetails(database, null, progressListener);

        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/integrationTesting/schemaMetaIT/input/noTableComment.xml","SchemaMetaIT", schema, false);
        Database databaseWithSchemaMeta = new Database(
                dbmsMeta,
                "SchemaMetaIT",
                catalog,
                schema
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(config).gatherSchemaDetails(databaseWithSchemaMeta, schemaMeta, progressListener);

        assertThat(database.getTables()).isNotEmpty();
        assertThat(database.getTablesMap().get("ACCOUNT").getColumn("accountId").getComments()).isNull();

        assertThat(databaseWithSchemaMeta.getTables()).isNotEmpty();
        assertThat(databaseWithSchemaMeta.getTablesMap().get("ACCOUNT").getColumn("accountId").getComments()).isEqualToIgnoringCase(BY_SCHEMA_META_COMMENT);
    }

    @Test
    public void commentsAreReplacedWithReplaceComments() throws Exception {
        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/integrationTesting/schemaMetaIT/input/replaceComments.xml","SchemaMetaIT", schema, false);
        Database database = new Database(
                dbmsMeta,
                "SchemaMetaIT",
                catalog,
                schema
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(config).gatherSchemaDetails(database, schemaMeta, progressListener);

        assertThat(database.getTables()).isNotEmpty();
        assertThat(database.getSchema().getComment()).isEqualToIgnoringCase(BY_SCHEMA_META_COMMENT);
        assertThat(database.getTablesMap().get("ACCOUNT").getComments()).isEqualToIgnoringCase(BY_SCHEMA_META_COMMENT);
        assertThat(database.getTablesMap().get("ACCOUNT").getColumn("name").getComments()).isEqualToIgnoringCase(BY_SCHEMA_META_COMMENT);
    }

    @Test
    public void remoteTable() throws Exception {
        Database database = new Database(
                dbmsMeta,
                "SchemaMetaIT",
                catalog,
                schema
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(config).gatherSchemaDetails(database, null, progressListener);

        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/integrationTesting/schemaMetaIT/input/remoteTable.xml","SchemaMetaIT", schema, false);
        Database databaseWithSchemaMeta = new Database(
                dbmsMeta,
                "SchemaMetaIT",
                catalog,
                schema
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(config).gatherSchemaDetails(databaseWithSchemaMeta, schemaMeta, progressListener);

        assertThat(database.getRemoteTables()).hasSizeLessThan(databaseWithSchemaMeta.getRemoteTables().size());
        assertThat(database.getRemoteTablesMap().get("other.other.CONTRACT")).isNull();
        assertThat(databaseWithSchemaMeta.getRemoteTablesMap().get("other.other.CONTRACT")).isNotNull();
    }

    @Test
    public void remoteTableAndRelationShip() throws Exception {
        Database database = new Database(
                dbmsMeta,
                "SchemaMetaIT",
                catalog,
                schema
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(config).gatherSchemaDetails(database, null, progressListener);

        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/integrationTesting/schemaMetaIT/input/remoteTable.xml","SchemaMetaIT", schema, false);
        Database databaseWithSchemaMeta = new Database(
                dbmsMeta,
                "SchemaMetaIT",
                catalog,
                schema
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(config).gatherSchemaDetails(databaseWithSchemaMeta, schemaMeta, progressListener);

        assertThat(database.getTablesMap().get("ACCOUNT").getNumChildren())
                .isLessThan(databaseWithSchemaMeta.getTablesMap().get("ACCOUNT").getNumChildren());
    }

    @Test
    public void addColumn() throws Exception {
        Database database = new Database(
                dbmsMeta,
                "SchemaMetaIT",
                catalog,
                schema
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(config).gatherSchemaDetails(database, null, progressListener);

        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/integrationTesting/schemaMetaIT/input/addColumn.xml","SchemaMetaIT", schema, false);
        Database databaseWithSchemaMeta = new Database(
                dbmsMeta,
                "SchemaMetaIT",
                catalog,
                schema
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(config).gatherSchemaDetails(databaseWithSchemaMeta, schemaMeta, progressListener);

        assertThat(database.getTablesMap().get("ACCOUNT").getColumns())
                .hasSizeLessThan(databaseWithSchemaMeta.getTablesMap().get("ACCOUNT").getColumns().size());
    }

    @Test
    public void disableImpliedOnAgentAccountId() throws Exception {
        Database database = new Database(
                dbmsMeta,
                "SchemaMetaIT",
                catalog,
                schema
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(config).gatherSchemaDetails(database, null, progressListener);

        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/integrationTesting/schemaMetaIT/input/disableImpliedOnAgent.xml","SchemaMetaIT", schema, false);
        Database databaseWithSchemaMeta = new Database(
                dbmsMeta,
                "SchemaMetaIT",
                catalog,
                schema
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(config).gatherSchemaDetails(databaseWithSchemaMeta, schemaMeta, progressListener);

        new ImpliedConstraintsFinder().find(database.getTables());
        new ImpliedConstraintsFinder().find(databaseWithSchemaMeta.getTables());

        assertThat(database.getTablesMap().get("ACCOUNT").getNumChildren())
                .isGreaterThan(databaseWithSchemaMeta.getTablesMap().get("ACCOUNT").getNumChildren());
    }

    @Test
    public void addFKInsteadOfImplied() throws Exception {
        Database database = new Database(
                dbmsMeta,
                "SchemaMetaIT",
                catalog,
                schema
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(config).gatherSchemaDetails(database, null, progressListener);

        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/integrationTesting/schemaMetaIT/input/addFKInsteadOfImplied.xml","SchemaMetaIT", schema, false);
        Database databaseWithSchemaMeta = new Database(
                dbmsMeta,
                "SchemaMetaIT",
                catalog,
                schema
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(config).gatherSchemaDetails(databaseWithSchemaMeta, schemaMeta, progressListener);

        assertThat(database.getTablesMap().get("ACCOUNT").getNumChildren())
                .isLessThan(databaseWithSchemaMeta.getTablesMap().get("ACCOUNT").getNumChildren());
    }

    @Test
    public void disableDiagramAssociations() throws Exception {
        Database database = new Database(
                dbmsMeta,
                "SchemaMetaIT",
                catalog,
                schema
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(config).gatherSchemaDetails(database, null, progressListener);

        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/integrationTesting/schemaMetaIT/input/disableDiagramAssociations.xml","SchemaMetaIT", schema, false);
        Database databaseWithSchemaMeta = new Database(
                dbmsMeta,
                "SchemaMetaIT",
                catalog,
                schema
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(config).gatherSchemaDetails(databaseWithSchemaMeta, schemaMeta, progressListener);

        DotFormatter dotFormatter = new DotFormatter(
            new SimpleDotConfig(
                new DefaultFontConfig(
                    config.getFont(),
                    config.getFontSize()
                ),
                config.isRankDirBugEnabled(),
                false,
                config.isNumRowsEnabled(),
                config.isOneOfMultipleSchemas()
            )
        );

        StringWriter withoutSchemaMetaOutput = new StringWriter();
        try (PrintWriter printWriter = new PrintWriter(withoutSchemaMetaOutput)) {
            dotFormatter.writeTableAllRelationships(database.getTablesMap().get("COMPANY"), false, new LongAdder(), printWriter);
        }
        StringWriter withSchemaMetaOutput = new StringWriter();
        try (PrintWriter printWriter = new PrintWriter(withSchemaMetaOutput)){
            dotFormatter.writeTableAllRelationships(databaseWithSchemaMeta.getTablesMap().get("COMPANY"), false, new LongAdder(), printWriter);
        }
        assertThat(withoutSchemaMetaOutput.toString()).contains("\"COUNTRY\":\"COUNTRYID\"");
        assertThat(withSchemaMetaOutput.toString()).doesNotContain("\"COUNTRY\":\"COUNTRYID\"");
    }
}
