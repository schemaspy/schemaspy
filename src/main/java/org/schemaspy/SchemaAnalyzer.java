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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.schemaspy.analyzer.ImpliedConstraintsFinder;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.input.dbms.CatalogResolver;
import org.schemaspy.input.dbms.DbDriverLoader;
import org.schemaspy.input.dbms.SchemaResolver;
import org.schemaspy.input.dbms.service.DatabaseService;
import org.schemaspy.input.dbms.service.DatabaseServiceFactory;
import org.schemaspy.input.dbms.service.SqlService;
import org.schemaspy.input.dbms.xml.SchemaMeta;
import org.schemaspy.model.*;
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
import org.schemaspy.util.*;
import org.schemaspy.util.copy.CopyFromUrl;
import org.schemaspy.util.naming.FileNameGenerator;
import org.schemaspy.view.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.*;
import java.util.stream.Collectors;

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
    private static final int SECONDS_IN_MS = 1000;
    private static final String DOT_HTML = ".html";
    private static final String INDEX_DOT_HTML = "index.html";

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
        ProgressListener progressListener = new Tracked();
        // don't render console-based detail unless we're generating HTML (those probably don't have a user watching)
        // and not already logging fine details (to keep from obfuscating those)
        if (commandLineArguments.isHtmlEnabled()) {
            progressListener = new Console(commandLineArguments, progressListener);
        }

        if (commandLineArguments.isEvaluateAllEnabled() || !commandLineArguments.getSchemas().isEmpty()) {
            return this.analyzeMultipleSchemas(
                    databaseServiceFactory.forMultipleSchemas(commandLineArguments.getProcessingConfig()),
                    progressListener
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
                    databaseServiceFactory.forSingleSchema(commandLineArguments.getProcessingConfig()),
                    progressListener
            );
        }
    }

    public Database analyzeMultipleSchemas(
            DatabaseService databaseService,
            ProgressListener progressListener
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
                    schemaSpec);
            schemas = DbAnalyzer.getPopulatedSchemas(meta, schemaSpec, false);
            if (schemas.isEmpty())
                schemas = DbAnalyzer.getPopulatedSchemas(meta, schemaSpec, true);
            if (schemas.isEmpty())
                schemas.add(commandLineArguments.getConnectionConfig().getUser());
        }

        LOGGER.info("Analyzing schemas: {}{}",
                System.lineSeparator(),
                schemas.stream().map(s -> String.format("'%s'", s)).collect(Collectors.joining(System.lineSeparator())));

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

            LOGGER.info("Analyzing '{}'", schema);
            File outputDirForSchema = new File(outputDir, new FileNameGenerator(schema).value());
            db = this.analyze(dbName, schema, true, outputDirForSchema, databaseService, progressListener);
            if (db == null) //if any of analysed schema returns null
                return null;
            mustacheSchemas.add(new MustacheSchema(db.getSchema(), ""));
            mustacheCatalog = new MustacheCatalog(db.getCatalog(), "");
        }

        new CopyFromUrl(layoutFolder.url(), outputDir, notHtml()).copy();

        DataTableConfig dataTableConfig = new DataTableConfig(commandLineArguments);
        MustacheCompiler mustacheCompiler = new MustacheCompiler(
            commandLineArguments.getConnectionConfig().getDatabaseName(),
            null,
            commandLineArguments.getHtmlConfig(),
            true,
            dataTableConfig
        );
        HtmlMultipleSchemasIndexPage htmlMultipleSchemasIndexPage = new HtmlMultipleSchemasIndexPage(mustacheCompiler);
        try (Writer writer = new DefaultPrintWriter(outputDir.toPath().resolve(INDEX_DOT_HTML).toFile())) {
            htmlMultipleSchemasIndexPage.write(
                mustacheCatalog,
                mustacheSchemas,
                commandLineArguments.getHtmlConfig().getDescription(),
                getDatabaseProduct(meta),
                writer
            );
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
            DatabaseService databaseService,
            ProgressListener progressListener
    ) throws SQLException, IOException {
        LOGGER.info("Starting schema analysis");

        FileUtils.forceMkdir(outputDir);

        String catalog = commandLineArguments.getCatalog();

        DatabaseMetaData databaseMetaData = sqlService.connect(commandLineArguments.getConnectionConfig());
        DbmsMeta dbmsMeta = sqlService.getDbmsMeta();

        LOGGER.debug("supportsSchemasInTableDefinitions: {}", databaseMetaData.supportsSchemasInTableDefinitions());
        LOGGER.debug("supportsCatalogsInTableDefinitions: {}", databaseMetaData.supportsCatalogsInTableDefinitions());

        // set default Catalog and Schema of the connection
        catalog = new CatalogResolver(databaseMetaData).resolveCatalog(catalog);
        schema = new SchemaResolver(databaseMetaData).resolveSchema(schema);

        SchemaMeta schemaMeta = commandLineArguments.getSchemaMeta() == null ? null : new SchemaMeta(commandLineArguments.getSchemaMeta(), dbName, schema, isOneOfMultipleSchemas);
        if (commandLineArguments.isHtmlEnabled()) {
            FileUtils.forceMkdir(new File(outputDir, "tables"));
            FileUtils.forceMkdir(new File(outputDir, "diagrams/summary"));

            LOGGER.info("Connected to {} - {}", databaseMetaData.getDatabaseProductName(), databaseMetaData.getDatabaseProductVersion());

            if (schemaMeta != null && schemaMeta.getFile() != null) {
                LOGGER.info("Using additional metadata from {}", schemaMeta.getFile());
            }
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

        long duration = progressListener.startedGraphingSummaries();
        if (commandLineArguments.isHtmlEnabled()) {
            generateHtmlDoc(
                    schema,
                    isOneOfMultipleSchemas,
                    commandLineArguments.useVizJS(),
                    progressListener,
                    outputDir,
                    db,
                    duration,
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

        duration = progressListener.finishedGatheringDetails();
        long overallDuration = progressListener.finished(tables);

        if (commandLineArguments.isHtmlEnabled()) {
            LOGGER.info("Wrote table details in {} seconds", duration / SECONDS_IN_MS);

            LOGGER.info("Wrote relationship details of {} tables/views to directory '{}' in {} seconds.", tables.size(), outputDir, overallDuration / SECONDS_IN_MS);
            LOGGER.info("View the results by opening {}", new File(outputDir, INDEX_DOT_HTML));
        }

        return db;
    }

    private void generateHtmlDoc(
            String schema,
            boolean isOneOfMultipleSchemas,
            boolean useVizJS,
            ProgressListener progressListener,
            File outputDir,
            Database db,
            long duration,
            Collection<Table> tables
    ) throws IOException {
        LOGGER.info("Gathered schema details in {} seconds", duration / SECONDS_IN_MS);
        LOGGER.info("Writing/graphing summary");

        Markdown.registryPage(tables);

        new CopyFromUrl(layoutFolder.url(), outputDir, notHtml()).copy();

        Renderer renderer = useVizJS ? new VizJSDot() : new GraphvizDot(commandLineArguments.getGraphVizConfig());

        Path htmlInfoFile = outputDir.toPath().resolve("info-html.txt");
        Files.deleteIfExists(htmlInfoFile);
        writeInfo("date", ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssZ")), htmlInfoFile);
        writeInfo("os", System.getProperty("os.name") + " " + System.getProperty("os.version"), htmlInfoFile);
        writeInfo("schemaspy-version", ManifestUtils.getImplementationVersion(), htmlInfoFile);
        writeInfo("schemaspy-build", ManifestUtils.getImplementationBuild(), htmlInfoFile);
        writeInfo("renderer", renderer.identifier(), htmlInfoFile);
        progressListener.graphingSummaryProgressed();

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

        DotFormatter dotProducer = new DotFormatter(runtimeDotConfig);

        File diagramDir = new File(outputDir, "diagrams");
        diagramDir.mkdirs();
        File summaryDir = new File(diagramDir, "summary");
        summaryDir.mkdirs();
        SummaryDiagram summaryDiagram = new SummaryDiagram(renderer, summaryDir);


        MustacheSummaryDiagramFactory mustacheSummaryDiagramFactory = new MustacheSummaryDiagramFactory(dotProducer, summaryDiagram, hasRealConstraints, !impliedConstraints.isEmpty() , outputDir);
        MustacheSummaryDiagramResults results = mustacheSummaryDiagramFactory.generateSummaryDiagrams(db, tables, progressListener);
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
        }

        progressListener.graphingSummaryProgressed();

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
        }

        progressListener.graphingSummaryProgressed();

        HtmlMainIndexPage htmlMainIndexPage = new HtmlMainIndexPage(
            mustacheCompiler,
            commandLineArguments.getHtmlConfig().getDescription()
        );
        try (Writer writer = new DefaultPrintWriter(outputDir.toPath().resolve(INDEX_DOT_HTML).toFile())) {
            htmlMainIndexPage.write(db, tables, impliedConstraints, writer);
        }

        progressListener.graphingSummaryProgressed();

        List<ForeignKeyConstraint> constraints = DbAnalyzer.getForeignKeyConstraints(tables);

        HtmlConstraintsPage htmlConstraintsPage = new HtmlConstraintsPage(mustacheCompiler);
        try (Writer writer = new DefaultPrintWriter(outputDir.toPath().resolve("constraints.html").toFile())) {
            htmlConstraintsPage.write(constraints, tables, writer);
        }

        progressListener.graphingSummaryProgressed();

        HtmlAnomaliesPage htmlAnomaliesPage = new HtmlAnomaliesPage(mustacheCompiler);
        try (Writer writer = new DefaultPrintWriter(outputDir.toPath().resolve("anomalies.html").toFile())) {
            htmlAnomaliesPage.write(tables, impliedConstraints, writer);
        }

        progressListener.graphingSummaryProgressed();

        HtmlColumnsPage htmlColumnsPage = new HtmlColumnsPage(mustacheCompiler);
        try (Writer writer = new DefaultPrintWriter(outputDir.toPath().resolve("columns.html").toFile())) {
            htmlColumnsPage.write(tables, writer);
        }

        progressListener.graphingSummaryProgressed();

        HtmlRoutinesPage htmlRoutinesPage = new HtmlRoutinesPage(mustacheCompiler);
        try (Writer writer = new DefaultPrintWriter(outputDir.toPath().resolve("routines.html").toFile())) {
            htmlRoutinesPage.write(db.getRoutines(), writer);
        }

        HtmlRoutinePage htmlRoutinePage = new HtmlRoutinePage(mustacheCompiler);
        for (Routine routine : db.getRoutines()) {
            try (Writer writer = new DefaultPrintWriter(outputDir.toPath().resolve("routines").resolve(new FileNameGenerator(routine.getName()).value() + DOT_HTML).toFile())) {
                htmlRoutinePage.write(routine, writer);
            }
        }

        // create detailed diagrams

        duration = progressListener.startedGraphingDetails();

        LOGGER.info("Completed summary in {} seconds", duration / SECONDS_IN_MS);
        LOGGER.info("Writing/diagramming details");
        SqlAnalyzer sqlAnalyzer = new SqlAnalyzer(db.getDbmsMeta().getIdentifierQuoteString(), db.getDbmsMeta().getAllKeywords(), db.getTables(), db.getViews());

        File tablesDir = new File(diagramDir, "tables");
        tablesDir.mkdirs();
        TableDiagram tableDiagram = new TableDiagram(renderer, tablesDir);
        MustacheTableDiagramFactory mustacheTableDiagramFactory = new MustacheTableDiagramFactory(dotProducer, tableDiagram, outputDir, commandLineArguments.getDegreeOfSeparation());
        HtmlTablePage htmlTablePage = new HtmlTablePage(mustacheCompiler, sqlAnalyzer);
        for (Table table : tables) {
            List<MustacheTableDiagram> mustacheTableDiagrams = mustacheTableDiagramFactory.generateTableDiagrams(table);
            progressListener.graphingDetailsProgressed(table);
            LOGGER.debug("Writing details of {}", table.getName());
            try (Writer writer = new DefaultPrintWriter(outputDir.toPath().resolve("tables").resolve(new FileNameGenerator(table.getName()).value() + DOT_HTML).toFile())) {
                htmlTablePage.write(table, mustacheTableDiagrams, writer);
            }
        }
    }

    private FileFilter notHtml() {
        IOFileFilter notHtmlFilter = FileFilterUtils.notFileFilter(FileFilterUtils.suffixFileFilter(DOT_HTML));
        return FileFilterUtils.and(notHtmlFilter);
    }

    private static void writeInfo(String key, String value, Path infoFile) {
        try {
            Files.write(infoFile, (key + "=" + value + "\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
        } catch (IOException e) {
            LOGGER.error("Failed to write '{}', to '{}'", key + "=" + value, infoFile, e);
        }
    }

    /**
     * dumpNoDataMessage
     *
     * @param schema String
     * @param user   String
     * @param meta   DatabaseMetaData
     */
    private static void dumpNoTablesMessage(String schema, String user, DatabaseMetaData meta, boolean specifiedInclusions) throws SQLException {
        LOGGER.warn("No tables or views were found in schema '{}'.", schema);
        List<String> schemas;
        try {
            schemas = DbAnalyzer.getSchemas(meta);
        } catch (SQLException | RuntimeException exc) {
            LOGGER.error("The user you specified '{}' might not have rights to read the database metadata.", user, exc);
            return;
        }

        if (Objects.isNull(schemas)) {
            LOGGER.error("Failed to retrieve any schemas");
        } else if (schemas.contains(schema)) {
            LOGGER.error(
                    "The schema exists in the database, but the user you specified '{}'" +
                    "might not have rights to read its contents.",
                    user);
            if (specifiedInclusions) {
                LOGGER.error(
                        "Another possibility is that the regular expression that you specified " +
                        "for what to include (via -i) didn't match any tables.");
            }
        } else {
            LOGGER.error(
                    "The schema '{}' could not be read/found, schema is specified using the -s option." +
                    "Make sure user '{}' has the correct privileges to read the schema." +
                    "Also not that schema names are usually case sensitive.",
                    schema, user);
            LOGGER.info(
                    "Available schemas(Some of these may be user or system schemas):{}{}",
                    System.lineSeparator(),
                    schemas.stream().collect(Collectors.joining(System.lineSeparator())));
            List<String> populatedSchemas = DbAnalyzer.getPopulatedSchemas(meta);
            if (populatedSchemas.isEmpty()) {
                LOGGER.error("Unable to determine if any of the schemas contain tables/views");
            } else {
                LOGGER.info("Schemas with tables/views visible to '{}':{}{}",
                        user,
                        System.lineSeparator(),
                        populatedSchemas.stream().map(s -> String.format("'%s'", s)).collect(Collectors.joining(System.lineSeparator())));
            }
        }
    }
}
