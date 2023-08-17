/*
 * Copyright (C) 2004-2011 John Currier
 * Copyright (C) 2016, 2017 Rafal Kasa
 * Copyright (C) 2016, 2017 Ismail Simsek
 * Copyright (C) 2017 Wojciech Kasa
 * Copyright (C) 2017 Thomas Traude
 * Copyright (C) 2017, 2018 Nils Petzaell
 * Copyright (C) 2017 Daniel Watt
 *
 * This file is a part of the SchemaSpy project (http://schemaspy.org).
 *
 * SchemaSpy is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.schemaspy;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.schemaspy.analyzer.ImpliedConstraintsFinder;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.input.dbms.CatalogResolver;
import org.schemaspy.input.dbms.DbDriverLoader;
import org.schemaspy.input.dbms.SchemaResolver;
import org.schemaspy.input.dbms.service.DatabaseService;
import org.schemaspy.input.dbms.service.DatabaseServiceFactory;
import org.schemaspy.input.dbms.service.SqlService;
import org.schemaspy.input.dbms.xml.SchemaMeta;
import org.schemaspy.logging.Sanitize;
import org.schemaspy.model.Console;
import org.schemaspy.model.Database;
import org.schemaspy.model.DbmsMeta;
import org.schemaspy.model.EmptySchemaException;
import org.schemaspy.model.ForeignKeyConstraint;
import org.schemaspy.model.ImpliedForeignKeyConstraint;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.model.Routine;
import org.schemaspy.model.Table;
import org.schemaspy.model.Tracked;
import org.schemaspy.output.OutputException;
import org.schemaspy.output.OutputProducer;
import org.schemaspy.output.diagram.Renderer;
import org.schemaspy.output.diagram.SummaryDiagram;
import org.schemaspy.output.diagram.TableDiagram;
import org.schemaspy.output.diagram.graphviz.GraphvizDot;
import org.schemaspy.output.diagram.vizjs.VizJSDot;
import org.schemaspy.output.dot.RuntimeDotConfig;
import org.schemaspy.output.dot.schemaspy.DefaultFontConfig;
import org.schemaspy.output.dot.schemaspy.DotFormatter;
import org.schemaspy.output.dot.schemaspy.OrphanGraph;
import org.schemaspy.output.html.mustache.diagrams.MustacheSummaryDiagramFactory;
import org.schemaspy.output.html.mustache.diagrams.MustacheSummaryDiagramResults;
import org.schemaspy.output.html.mustache.diagrams.MustacheTableDiagramFactory;
import org.schemaspy.output.html.mustache.diagrams.OrphanDiagram;
import org.schemaspy.util.DataTableConfig;
import org.schemaspy.util.DefaultPrintWriter;
import org.schemaspy.util.ManifestUtils;
import org.schemaspy.util.Markdown;
import org.schemaspy.util.copy.CopyFromUrl;
import org.schemaspy.util.naming.FileNameGenerator;
import org.schemaspy.view.HtmlAnomaliesPage;
import org.schemaspy.view.HtmlColumnsPage;
import org.schemaspy.view.HtmlConstraintsPage;
import org.schemaspy.view.HtmlMainIndexPage;
import org.schemaspy.view.HtmlMultipleSchemasIndexPage;
import org.schemaspy.view.HtmlOrphansPage;
import org.schemaspy.view.HtmlRelationshipsPage;
import org.schemaspy.view.HtmlRoutinePage;
import org.schemaspy.view.HtmlRoutinesPage;
import org.schemaspy.view.HtmlTablePage;
import org.schemaspy.view.MustacheCatalog;
import org.schemaspy.view.MustacheCompiler;
import org.schemaspy.view.MustacheSchema;
import org.schemaspy.view.MustacheTableDiagram;
import org.schemaspy.view.SqlAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author John Currier
 * @author Rafal Kasa
 * @author Ismail Simsek
 * @author Wojciech Kasa
 * @author Thomas Traude
 * @author Nils Petzaell
 * @author Daniel Watt
 */
public class SchemaAnalyzer {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String DOT_HTML = ".html";
    private static final String INDEX_DOT_HTML = "index.html";
    private static final String LOG_SCHEMAS_FORMAT = "\t'{}'";

    private final SqlService sqlService;
    private final DatabaseServiceFactory databaseServiceFactory;

    private final CommandLineArguments commandLineArguments;

    private final OutputProducer outputProducer;

    private final LayoutFolder layoutFolder;

    public SchemaAnalyzer(
            final SqlService sqlService,
            final DatabaseServiceFactory databaseServiceFactory,
            final CommandLineArguments commandLineArguments,
            final OutputProducer outputProducer,
            final LayoutFolder layoutFolder
    ) {
        this.sqlService = Objects.requireNonNull(sqlService);
        this.databaseServiceFactory = databaseServiceFactory;
        this.commandLineArguments = Objects.requireNonNull(commandLineArguments);
        this.outputProducer = outputProducer;
        this.layoutFolder = layoutFolder;
    }

    public Database analyze() throws SQLException, IOException {

        if (commandLineArguments.isEvaluateAllEnabled() || !commandLineArguments.getSchemas().isEmpty()) {
            return this.analyzeMultipleSchemas(
                    databaseServiceFactory.forMultipleSchemas(commandLineArguments.getProcessingConfig())
            );
        } else {
            File outputDirectory = commandLineArguments.getOutputDirectory();
            Objects.requireNonNull(outputDirectory);
            String schema = commandLineArguments.getSchema();
            return analyze(
                    commandLineArguments.getConnectionConfig().getDatabaseName(),
                    schema,
                    false,
                    outputDirectory,
                    databaseServiceFactory.forSingleSchema(commandLineArguments.getProcessingConfig())
            );
        }
    }

    public Database analyzeMultipleSchemas(
            DatabaseService databaseService
    ) throws SQLException, IOException {
        List<String> schemas = commandLineArguments.getSchemas();
        Database db = null;

        Connection connection = new DbDriverLoader(commandLineArguments.getConnectionConfig()).getConnection();
        DatabaseMetaData meta = connection.getMetaData();
        if (schemas.isEmpty()) {
            String schemaSpec = commandLineArguments.getSchemaSpec();
            LOGGER.info(
                    "Analyzing schemas that match regular expression '{}'. " +
                    "(use -schemaSpec on command line or in .properties to exclude other schemas)",
                    new Sanitize(schemaSpec));
            schemas = DbAnalyzer.getPopulatedSchemas(meta, schemaSpec);
            if (schemas.isEmpty() && Objects.nonNull(commandLineArguments.getConnectionConfig().getUser())) {
                schemas.add(commandLineArguments.getConnectionConfig().getUser());
            }
            if (schemas.isEmpty()) {
                LOGGER.error("Couldn't find any schemas to analyze using schemaSpec '{}'", new Sanitize(schemaSpec));
                return null;
            }
        }

        LOGGER.info("Analyzing schemas:");
        schemas.forEach(
            schemaName -> LOGGER.info(
                LOG_SCHEMAS_FORMAT,
                new Sanitize(schemaName)
            )
        );

        File outputDir = commandLineArguments.getOutputDirectory();

        List<MustacheSchema> mustacheSchemas = new ArrayList<>();
        MustacheCatalog mustacheCatalog = null;
        for (String schema : schemas) {
            String dbName = Objects
                .nonNull(
                    commandLineArguments.getConnectionConfig().getDatabaseName()
                )
                ? commandLineArguments.getConnectionConfig().getDatabaseName()
                : schema;

            LOGGER.info("Analyzing '{}'", new Sanitize(schema));
            File outputDirForSchema = new File(outputDir, new FileNameGenerator(schema).value());
            db = this.analyze(dbName, schema, true, outputDirForSchema, databaseService);
            if (db == null) //if any of analysed schema returns null
                return null;
            mustacheSchemas.add(new MustacheSchema(db.getSchema(), ""));
            mustacheCatalog = new MustacheCatalog(db.getCatalog(), "");
        }

        if (commandLineArguments.isHtmlEnabled()) {
            new CopyFromUrl(layoutFolder.url(), outputDir, notHtml()).copy();

            DataTableConfig dataTableConfig = new DataTableConfig(commandLineArguments);
            MustacheCompiler mustacheCompiler = new MustacheCompiler(commandLineArguments.getConnectionConfig().getDatabaseName(),
                null, commandLineArguments.getHtmlConfig(), true, dataTableConfig
            );
            HtmlMultipleSchemasIndexPage htmlMultipleSchemasIndexPage = new HtmlMultipleSchemasIndexPage(
                mustacheCompiler);
            try (Writer writer = new DefaultPrintWriter(outputDir.toPath().resolve(INDEX_DOT_HTML).toFile())) {
                htmlMultipleSchemasIndexPage.write(mustacheCatalog, mustacheSchemas,
                    commandLineArguments.getHtmlConfig().getDescription(), getDatabaseProduct(meta), writer
                );
            }
        }
        return db;
    }

    /**
     * Copy / paste from Database, but we can't use Database here...
     *
     * @param meta DatabaseMetaData
     * @return String
     */
    private static String getDatabaseProduct(DatabaseMetaData meta) {
        try {
            return meta.getDatabaseProductName() + " - " + meta.getDatabaseProductVersion();
        } catch (SQLException exc) {
            return "";
        }
    }

    public Database analyze(
            String dbName,
            String schema,
            boolean isOneOfMultipleSchemas,
            File outputDir,
            DatabaseService databaseService
    ) throws SQLException, IOException {
        LOGGER.info("Starting schema analysis");
        ProgressListener progressListener = new Console(outputDir, new Tracked());

        FileUtils.forceMkdir(outputDir);

        String catalog = commandLineArguments.getCatalog();

        DatabaseMetaData databaseMetaData = sqlService.connect(commandLineArguments.getConnectionConfig());
        DbmsMeta dbmsMeta = sqlService.getDbmsMeta();
        LOGGER.info("Connected to {} - {}", databaseMetaData.getDatabaseProductName(), databaseMetaData.getDatabaseProductVersion());

        LOGGER.debug("supportsSchemasInTableDefinitions: {}", databaseMetaData.supportsSchemasInTableDefinitions());
        LOGGER.debug("supportsCatalogsInTableDefinitions: {}", databaseMetaData.supportsCatalogsInTableDefinitions());

        // set default Catalog and Schema of the connection
        catalog = new CatalogResolver(databaseMetaData).resolveCatalog(catalog);
        schema = new SchemaResolver(databaseMetaData).resolveSchema(schema);

        SchemaMeta schemaMeta = commandLineArguments.getSchemaMeta() == null
            ? null
            : new SchemaMeta(
                commandLineArguments.getSchemaMeta(),
                dbName,
                schema,
                isOneOfMultipleSchemas
            );
        if (schemaMeta != null && schemaMeta.getFile() != null) {
            LOGGER.info("Using additional metadata from {}", schemaMeta.getFile());
        }

        //
        // create our representation of the database
        //
        Database db = new Database(dbmsMeta, dbName, catalog, schema);
        databaseService.gatherSchemaDetails(db, schemaMeta, progressListener);


        Collection<Table> tables = new ArrayList<>(db.getTables());
        tables.addAll(db.getViews());

        if (tables.isEmpty()) {
            dumpNoTablesMessage(
                schema,
                commandLineArguments.getConnectionConfig().getUser(),
                databaseMetaData,
                !".*".equals(commandLineArguments.getProcessingConfig().getTableInclusions().toString())
            );
            if (!isOneOfMultipleSchemas) // don't bail if we're doing the whole enchilada
                throw new EmptySchemaException();
        }

        if (commandLineArguments.isHtmlEnabled()) {
            generateHtmlDoc(
                    schema,
                    isOneOfMultipleSchemas,
                    commandLineArguments.useVizJS(),
                    progressListener,
                    outputDir,
                    db,
                    tables
            );
        }

        try {
            outputProducer.generate(db, outputDir);
        } catch (OutputException oe) {
            if (isOneOfMultipleSchemas) {
                LOGGER.warn("Failed to produce output", oe);
            } else {
                throw oe;
            }
        }

        // create an orderer to be able to determine insertion and deletion ordering of tables
        // side effect is that the RI relationships get trashed
        List<Table> orderedTables = new InsertionOrdered(db).getTablesOrderedByRI();

        new OrderingReport(outputDir, orderedTables).write();

        progressListener.finished(tables);

        return db;
    }

    private void generateHtmlDoc(
            String schema,
            boolean isOneOfMultipleSchemas,
            boolean useVizJS,
            ProgressListener progressListener,
            File outputDir,
            Database db,
            Collection<Table> tables
    ) throws IOException {

        FileUtils.forceMkdir(new File(outputDir, "tables"));
        FileUtils.forceMkdir(new File(outputDir, "diagrams/summary"));

        Markdown.registryPage(tables);

        new CopyFromUrl(layoutFolder.url(), outputDir, notHtml()).copy();

        Renderer renderer = useVizJS ? new VizJSDot() : new GraphvizDot(commandLineArguments.getGraphVizConfig());

        Path htmlInfoFile = outputDir.toPath().resolve("info-html.txt");
        Files.deleteIfExists(htmlInfoFile);
        writeInfo("date", ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssZ")), htmlInfoFile);
        writeInfo("os", System.getProperty("os.name") + " " + System.getProperty("os.version"), htmlInfoFile);
        writeInfo("schemaspy-version", ManifestUtils.getImplementationVersion(), htmlInfoFile);
        writeInfo("schemaspy-revision", ManifestUtils.getImplementationRevision(), htmlInfoFile);
        writeInfo("renderer", renderer.identifier(), htmlInfoFile);
        progressListener.startCreatingSummaries();

        boolean hasRealConstraints = !db.getRemoteTables().isEmpty() || tables.stream().anyMatch(table -> !table.isOrphan(false));

        // if evaluating a 'ruby on rails-based' database then connect the columns
        // based on RoR conventions
        // note that this is done before 'hasRealRelationships' gets evaluated so
        // we get a relationships ER diagram
        if (commandLineArguments.isRailsEnabled()) {
            DbAnalyzer.getRailsConstraints(db.getTablesMap());
        }

        ImpliedConstraintsFinder impliedConstraintsFinder = new ImpliedConstraintsFinder();
        // getting implied constraints has a side-effect of associating the parent/child tables, so don't do it
        // here unless they want that behavior
        List<ImpliedForeignKeyConstraint> impliedConstraints =
         commandLineArguments.withImpliedRelationships()
         ? impliedConstraintsFinder.find(tables)
         : Collections.emptyList();

        RuntimeDotConfig runtimeDotConfig = new SimpleRuntimeDotConfig(
            new DefaultFontConfig(
                commandLineArguments.getDotConfig()
            ),
            commandLineArguments.getDotConfig(),
            "svg".equalsIgnoreCase(renderer.format()),
            isOneOfMultipleSchemas
        );

        DotFormatter dotProducer = new DotFormatter(runtimeDotConfig, commandLineArguments.withOrphans());

        File diagramDir = new File(outputDir, "diagrams");
        diagramDir.mkdirs();
        File summaryDir = new File(diagramDir, "summary");
        summaryDir.mkdirs();
        SummaryDiagram summaryDiagram = new SummaryDiagram(renderer, summaryDir);


        MustacheSummaryDiagramFactory mustacheSummaryDiagramFactory =
            new MustacheSummaryDiagramFactory(
                dotProducer,
                summaryDiagram,
                hasRealConstraints,
                !impliedConstraints.isEmpty(),
                outputDir,
                progressListener
            );
        MustacheSummaryDiagramResults results = mustacheSummaryDiagramFactory.generateSummaryDiagrams(db, tables);
        results.getOutputExceptions().stream().forEachOrdered(exception ->
                LOGGER.error("RelationShipDiagramError", exception)
        );
        DataTableConfig dataTableConfig = new DataTableConfig(commandLineArguments);
        MustacheCompiler mustacheCompiler = new MustacheCompiler(
            db.getName(),
            schema,
            commandLineArguments.getHtmlConfig(),
            isOneOfMultipleSchemas,
            dataTableConfig
        );

        HtmlRelationshipsPage htmlRelationshipsPage = new HtmlRelationshipsPage(mustacheCompiler, hasRealConstraints, !impliedConstraints.isEmpty());
        try (Writer writer = new DefaultPrintWriter(outputDir.toPath().resolve("relationships.html").toFile())) {
            htmlRelationshipsPage.write(results, writer);
            progressListener.createdSummary();
        }

        HtmlOrphansPage htmlOrphansPage = new HtmlOrphansPage(
                mustacheCompiler,
                new OrphanDiagram(
                        new OrphanGraph(runtimeDotConfig, tables),
                        renderer,
                        outputDir
                )
        );
        try (Writer writer = new DefaultPrintWriter(outputDir.toPath().resolve("orphans.html").toFile())) {
            htmlOrphansPage.write(writer);
            progressListener.createdSummary();
        }

        HtmlMainIndexPage htmlMainIndexPage = new HtmlMainIndexPage(
            mustacheCompiler,
            commandLineArguments.getHtmlConfig().getDescription()
        );
        try (Writer writer = new DefaultPrintWriter(outputDir.toPath().resolve(INDEX_DOT_HTML).toFile())) {
            htmlMainIndexPage.write(db, tables, impliedConstraints, writer);
        }

        List<ForeignKeyConstraint> constraints = DbAnalyzer.getForeignKeyConstraints(tables);

        HtmlConstraintsPage htmlConstraintsPage = new HtmlConstraintsPage(mustacheCompiler);
        try (Writer writer = new DefaultPrintWriter(outputDir.toPath().resolve("constraints.html").toFile())) {
            htmlConstraintsPage.write(constraints, tables, writer);
        }

        HtmlAnomaliesPage htmlAnomaliesPage = new HtmlAnomaliesPage(mustacheCompiler);
        try (Writer writer = new DefaultPrintWriter(outputDir.toPath().resolve("anomalies.html").toFile())) {
            htmlAnomaliesPage.write(tables, impliedConstraints, writer);
        }

        HtmlColumnsPage htmlColumnsPage = new HtmlColumnsPage(mustacheCompiler);
        try (Writer writer = new DefaultPrintWriter(outputDir.toPath().resolve("columns.html").toFile())) {
            htmlColumnsPage.write(tables, writer);
        }

        HtmlRoutinesPage htmlRoutinesPage = new HtmlRoutinesPage(mustacheCompiler);
        try (Writer writer = new DefaultPrintWriter(outputDir.toPath().resolve("routines.html").toFile())) {
            htmlRoutinesPage.write(db.getRoutines(), writer);
        }

        HtmlRoutinePage htmlRoutinePage = new HtmlRoutinePage(mustacheCompiler);
        for (Routine routine : db.getRoutines()) {
            try (Writer writer = new DefaultPrintWriter(outputDir.toPath().resolve("routines").resolve(new FileNameGenerator(routine.getName()).value() + DOT_HTML).toFile())) {
                htmlRoutinePage.write(routine, writer);
                progressListener.createdSummary();
            }
        }

        progressListener.finishedCreatingSummaries();

        // create detailed diagrams

        progressListener.startCreatingTablePages();
        SqlAnalyzer sqlAnalyzer = new SqlAnalyzer(db.getDbmsMeta().getIdentifierQuoteString(), db.getDbmsMeta().getAllKeywords(), db.getTables(), db.getViews());

        File tablesDir = new File(diagramDir, "tables");
        tablesDir.mkdirs();
        TableDiagram tableDiagram = new TableDiagram(renderer, tablesDir);
        MustacheTableDiagramFactory mustacheTableDiagramFactory = new MustacheTableDiagramFactory(dotProducer, tableDiagram, outputDir, commandLineArguments.getDegreeOfSeparation());
        HtmlTablePage htmlTablePage = new HtmlTablePage(mustacheCompiler, sqlAnalyzer);
        for (Table table : tables) {
            List<MustacheTableDiagram> mustacheTableDiagrams = mustacheTableDiagramFactory.generateTableDiagrams(table);
            LOGGER.debug("Writing details of {}", table.getName());
            try (Writer writer = new DefaultPrintWriter(outputDir.toPath().resolve("tables").resolve(new FileNameGenerator(table.getName()).value() + DOT_HTML).toFile())) {
                htmlTablePage.write(table, mustacheTableDiagrams, writer);
                progressListener.createdTablePage(table);
            }
        }
        progressListener.finishedCreatingTablePages();
        LOGGER.info("View the results by opening {}", new File(outputDir, INDEX_DOT_HTML));
    }

    private FileFilter notHtml() {
        return FileFilterUtils
            .and(
                FileFilterUtils
                    .notFileFilter(
                        FileFilterUtils.suffixFileFilter(DOT_HTML)
                    )
            );
    }

    private static void writeInfo(String key, String value, Path infoFile) {
        try {
            Files.write(
                infoFile,
                (key + "=" + value + "\n").getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE)
            ;
        } catch (IOException e) {
            LOGGER.error(
                "Failed to write '{}', to '{}={}'",
                new Sanitize(key),
                new Sanitize(value),
                infoFile,
                e
            );
        }
    }

    /**
     * dumpNoDataMessage
     *
     * @param schema String
     * @param user   String
     * @param meta   DatabaseMetaData
     */
    private static void dumpNoTablesMessage(
        String schema,
        String user,
        DatabaseMetaData meta,
        boolean specifiedInclusions
    ) throws SQLException {
        LOGGER.warn(
            "No tables or views were found in schema '{}'.",
            new Sanitize(schema)
        );
        List<String> schemas;
        try {
            schemas = DbAnalyzer.getSchemas(meta);
        } catch (SQLException | RuntimeException exc) {
            LOGGER.error(
                "The user you specified '{}' might not have rights to read the database metadata.",
                new Sanitize(user),
                exc
            );
            return;
        }

        if (schemas.isEmpty()) {
            try {
                schemas = DbAnalyzer.getCatalogs(meta);
            } catch (SQLException | RuntimeException exc) {
                LOGGER.error(
                    "The user you specified '{}' might not have rights to read the database metadata.",
                    new Sanitize(user),
                    exc
                );
                return;
            }
        }

        if (schemas.contains(schema)) {
            LOGGER.error(
                    "The schema exists in the database, but the user you specified '{}'" +
                    "might not have rights to read its contents.",
                    new Sanitize(user)
            );
            if (specifiedInclusions) {
                LOGGER.error(
                        "Another possibility is that the regular expression that you specified " +
                        "for what to include (via -i) didn't match any tables.");
            }
        } else {
            LOGGER.error(
                "The schema '{}' could not be read/found, schema is specified using the -s option.",
                new Sanitize(schema)
            );
            LOGGER.error(
                "Make sure user '{}' has the correct privileges to read the schema.",
                new Sanitize(user)
            );
            LOGGER.error("Also not that schema names are usually case sensitive.");
            List<String> populatedSchemas = DbAnalyzer.getPopulatedSchemas(meta);
            if (populatedSchemas.isEmpty()) {
                LOGGER.error(
                    "Unable to determine if any of the schemas are visible to '{}'",
                    new Sanitize(user)
                );
            } else {
                LOGGER.info(
                    "Schemas with tables/views visible to '{}':",
                    new Sanitize(user)
                );
                populatedSchemas
                    .forEach(
                        schemaName -> LOGGER.info(
                            LOG_SCHEMAS_FORMAT,
                            new Sanitize(schemaName)
                        )
                    );
            }
            List<String> otherSchemas = schemas
                .stream()
                .filter(Predicate.not(populatedSchemas::contains))
                .toList();
            if (!otherSchemas.isEmpty()) {
                LOGGER.info("Other available schemas(Some of these may be system schemas):");
                otherSchemas
                    .forEach(
                        schemaName -> LOGGER.info(
                            LOG_SCHEMAS_FORMAT,
                            new Sanitize(schemaName)
                        )
                    );
            }
        }
    }
}
