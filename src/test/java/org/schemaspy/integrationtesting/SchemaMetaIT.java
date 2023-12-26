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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.schemaspy.SimpleRuntimeDotConfig;
import org.schemaspy.analyzer.ImpliedConstraintsFinder;
import org.schemaspy.cli.CommandLineArgumentParser;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.connection.ScSimple;
import org.schemaspy.input.dbms.ConnectionConfig;
import org.schemaspy.input.dbms.ConnectionURLBuilder;
import org.schemaspy.input.dbms.DriverFromConfig;
import org.schemaspy.input.dbms.service.DatabaseServiceFactory;
import org.schemaspy.input.dbms.service.SqlService;
import org.schemaspy.input.dbms.xml.SchemaMeta;
import org.schemaspy.model.Database;
import org.schemaspy.model.DbmsMeta;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.output.dot.schemaspy.DefaultFontConfig;
import org.schemaspy.output.dot.schemaspy.DotFormatter;
import org.schemaspy.testing.H2MemoryExtension;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.concurrent.atomic.LongAdder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author Nils Petzaell
 */
class SchemaMetaIT {

    private static final String BY_SCRIPT_COMMENT = "Set by script";
    private static final String BY_SCHEMA_META_COMMENT = "Set from SchemaMeta";

    @RegisterExtension
    static H2MemoryExtension h2 = new H2MemoryExtension("SchemaMetaIT")
            .addSqlScript("src/test/resources/integrationTesting/schemaMetaIT/dbScripts/shemaMetaIT.h2.sql");

    private final SqlService sqlService = new SqlService();

    private final ProgressListener progressListener = mock(ProgressListener.class);
    private CommandLineArguments commandLineArguments;
    private DbmsMeta dbmsMeta;
    private String schema;
    private String catalog;

    @BeforeEach
    void setup() throws IOException, SQLException {
        String[] args = {
                "-t", "src/test/resources/integrationTesting/dbTypes/h2memory",
                "-db", "SchemaMetaIT",
                "-s", "SCHEMAMETAIT",
                "-o", "target/integrationtesting/schemaMetaIT",
                "-u", "sa",
                "--no-orphans"
        };
        commandLineArguments = new CommandLineArgumentParser(
                args
        ).commandLineArguments();
        ConnectionConfig connectionConfig = commandLineArguments.getConnectionConfig();
        sqlService.connect(
            new ScSimple(
                connectionConfig,
                new ConnectionURLBuilder(connectionConfig),
                new DriverFromConfig(connectionConfig)
            )
        );
        dbmsMeta = sqlService.getDbmsMeta();
        schema = h2.getConnection().getSchema();
        catalog = h2.getConnection().getCatalog();
    }

    @Test
    void commentsNullTableComment() throws Exception {
        Database database = new Database(
                dbmsMeta,
                "DatabaseServiceIT",
                catalog,
                schema
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(commandLineArguments.getProcessingConfig()).gatherSchemaDetails(database, null, progressListener);

        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/integrationTesting/schemaMetaIT/input/nullTableComment.xml","SchemaMetaIT", schema, false);
        Database databaseWithSchemaMeta = new Database(
                dbmsMeta,
                "SchemaMetaIT",
                catalog,
                schema
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(commandLineArguments.getProcessingConfig()).gatherSchemaDetails(databaseWithSchemaMeta, schemaMeta, progressListener);

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
    void commentsNoTableComment() throws SQLException {
        Database database = new Database(
                dbmsMeta,
                "DatabaseServiceIT",
                catalog,
                schema
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(commandLineArguments.getProcessingConfig()).gatherSchemaDetails(database, null, progressListener);

        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/integrationTesting/schemaMetaIT/input/noTableComment.xml","SchemaMetaIT", schema, false);
        Database databaseWithSchemaMeta = new Database(
                dbmsMeta,
                "SchemaMetaIT",
                catalog,
                schema
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(commandLineArguments.getProcessingConfig()).gatherSchemaDetails(databaseWithSchemaMeta, schemaMeta, progressListener);

        assertThat(database.getTables()).isNotEmpty();
        assertThat(database.getTablesMap().get("ACCOUNT").getColumn("accountId").getComments()).isNull();

        assertThat(databaseWithSchemaMeta.getTables()).isNotEmpty();
        assertThat(databaseWithSchemaMeta.getTablesMap().get("ACCOUNT").getColumn("accountId").getComments()).isEqualToIgnoringCase(BY_SCHEMA_META_COMMENT);
    }

    @Test
    void commentsAreReplacedWithReplaceComments() throws Exception {
        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/integrationTesting/schemaMetaIT/input/replaceComments.xml","SchemaMetaIT", schema, false);
        Database database = new Database(
                dbmsMeta,
                "SchemaMetaIT",
                catalog,
                schema
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(commandLineArguments.getProcessingConfig()).gatherSchemaDetails(database, schemaMeta, progressListener);

        assertThat(database.getTables()).isNotEmpty();
        assertThat(database.getSchema().getComment()).isEqualToIgnoringCase(BY_SCHEMA_META_COMMENT);
        assertThat(database.getTablesMap().get("ACCOUNT").getComments()).isEqualToIgnoringCase(BY_SCHEMA_META_COMMENT);
        assertThat(database.getTablesMap().get("ACCOUNT").getColumn("name").getComments()).isEqualToIgnoringCase(BY_SCHEMA_META_COMMENT);
    }

    @Test
    void remoteTable() throws Exception {
        Database database = new Database(
                dbmsMeta,
                "SchemaMetaIT",
                catalog,
                schema
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(commandLineArguments.getProcessingConfig()).gatherSchemaDetails(database, null, progressListener);

        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/integrationTesting/schemaMetaIT/input/remoteTable.xml","SchemaMetaIT", schema, false);
        Database databaseWithSchemaMeta = new Database(
                dbmsMeta,
                "SchemaMetaIT",
                catalog,
                schema
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(commandLineArguments.getProcessingConfig()).gatherSchemaDetails(databaseWithSchemaMeta, schemaMeta, progressListener);

        assertThat(database.getRemoteTables()).hasSizeLessThan(databaseWithSchemaMeta.getRemoteTables().size());
        assertThat(database.getRemoteTablesMap().get("other.other.CONTRACT")).isNull();
        assertThat(databaseWithSchemaMeta.getRemoteTablesMap().get("other.other.CONTRACT")).isNotNull();
    }

    @Test
    void remoteTableAndRelationShip() throws Exception {
        Database database = new Database(
                dbmsMeta,
                "SchemaMetaIT",
                catalog,
                schema
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(commandLineArguments.getProcessingConfig()).gatherSchemaDetails(database, null, progressListener);

        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/integrationTesting/schemaMetaIT/input/remoteTable.xml","SchemaMetaIT", schema, false);
        Database databaseWithSchemaMeta = new Database(
                dbmsMeta,
                "SchemaMetaIT",
                catalog,
                schema
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(commandLineArguments.getProcessingConfig()).gatherSchemaDetails(databaseWithSchemaMeta, schemaMeta, progressListener);

        assertThat(database.getTablesMap().get("ACCOUNT").getNumChildren())
                .isLessThan(databaseWithSchemaMeta.getTablesMap().get("ACCOUNT").getNumChildren());
    }

    @Test
    void addColumn() throws Exception {
        Database database = new Database(
                dbmsMeta,
                "SchemaMetaIT",
                catalog,
                schema
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(commandLineArguments.getProcessingConfig()).gatherSchemaDetails(database, null, progressListener);

        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/integrationTesting/schemaMetaIT/input/addColumn.xml","SchemaMetaIT", schema, false);
        Database databaseWithSchemaMeta = new Database(
                dbmsMeta,
                "SchemaMetaIT",
                catalog,
                schema
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(commandLineArguments.getProcessingConfig()).gatherSchemaDetails(databaseWithSchemaMeta, schemaMeta, progressListener);

        assertThat(database.getTablesMap().get("ACCOUNT").getColumns())
                .hasSizeLessThan(databaseWithSchemaMeta.getTablesMap().get("ACCOUNT").getColumns().size());
    }

    @Test
    void disableImpliedOnAgentAccountId() throws Exception {
        Database database = new Database(
                dbmsMeta,
                "SchemaMetaIT",
                catalog,
                schema
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(commandLineArguments.getProcessingConfig()).gatherSchemaDetails(database, null, progressListener);

        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/integrationTesting/schemaMetaIT/input/disableImpliedOnAgent.xml","SchemaMetaIT", schema, false);
        Database databaseWithSchemaMeta = new Database(
                dbmsMeta,
                "SchemaMetaIT",
                catalog,
                schema
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(commandLineArguments.getProcessingConfig()).gatherSchemaDetails(databaseWithSchemaMeta, schemaMeta, progressListener);

        new ImpliedConstraintsFinder().find(database.getTables());
        new ImpliedConstraintsFinder().find(databaseWithSchemaMeta.getTables());

        assertThat(database.getTablesMap().get("ACCOUNT").getNumChildren())
                .isGreaterThan(databaseWithSchemaMeta.getTablesMap().get("ACCOUNT").getNumChildren());
    }

    @Test
    void addFKInsteadOfImplied() throws Exception {
        Database database = new Database(
                dbmsMeta,
                "SchemaMetaIT",
                catalog,
                schema
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(commandLineArguments.getProcessingConfig()).gatherSchemaDetails(database, null, progressListener);

        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/integrationTesting/schemaMetaIT/input/addFKInsteadOfImplied.xml","SchemaMetaIT", schema, false);
        Database databaseWithSchemaMeta = new Database(
                dbmsMeta,
                "SchemaMetaIT",
                catalog,
                schema
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(commandLineArguments.getProcessingConfig()).gatherSchemaDetails(databaseWithSchemaMeta, schemaMeta, progressListener);

        assertThat(database.getTablesMap().get("ACCOUNT").getNumChildren())
                .isLessThan(databaseWithSchemaMeta.getTablesMap().get("ACCOUNT").getNumChildren());
    }

    @Test
    void disableDiagramAssociations() throws Exception {
        Database database = new Database(
                dbmsMeta,
                "SchemaMetaIT",
                catalog,
                schema
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(commandLineArguments.getProcessingConfig()).gatherSchemaDetails(database, null, progressListener);

        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/integrationTesting/schemaMetaIT/input/disableDiagramAssociations.xml","SchemaMetaIT", schema, false);
        Database databaseWithSchemaMeta = new Database(
                dbmsMeta,
                "SchemaMetaIT",
                catalog,
                schema
        );
        new DatabaseServiceFactory(sqlService).forSingleSchema(commandLineArguments.getProcessingConfig()).gatherSchemaDetails(databaseWithSchemaMeta, schemaMeta, progressListener);

        DotFormatter dotFormatter = new DotFormatter(
            new SimpleRuntimeDotConfig(
                new DefaultFontConfig(
                    "Helvetica",
                    11
                ),
                commandLineArguments.getDotConfig(),
                false,
                false
            ),
            commandLineArguments.withOrphans()
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
