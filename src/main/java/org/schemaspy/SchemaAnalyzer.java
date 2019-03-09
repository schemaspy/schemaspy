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
import org.schemaspy.db.CatalogResolver;
import org.schemaspy.db.SchemaResolver;
import org.schemaspy.model.*;
import org.schemaspy.model.xml.SchemaMeta;
import org.schemaspy.output.OutputException;
import org.schemaspy.output.OutputProducer;
import org.schemaspy.output.diagram.DiagramFactory;
import org.schemaspy.output.diagram.graphviz.GraphvizDot;
import org.schemaspy.output.diagram.vizjs.VizJSDot;
import org.schemaspy.output.html.mustache.diagrams.*;
import org.schemaspy.output.xml.dom.XmlProducerUsingDOM;
import org.schemaspy.service.DatabaseService;
import org.schemaspy.service.SqlService;
import org.schemaspy.util.ManifestUtils;
import org.schemaspy.util.ResourceWriter;
import org.schemaspy.util.Writers;
import org.schemaspy.view.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.net.URL;
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
 * @author Ben Hartwich
 */
public class SchemaAnalyzer {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final int SECONDS_IN_MS = 1000;
    private static final String DOT_HTML = ".html";
    private static final String INDEX_DOT_HTML = "index.html";


    private final SqlService sqlService;

    private final DatabaseService databaseService;

    private final CommandLineArguments commandLineArguments;

    private final List<OutputProducer> outputProducers = new ArrayList<>();

    public SchemaAnalyzer(SqlService sqlService, DatabaseService databaseService, CommandLineArguments commandLineArguments) {
        this.sqlService = Objects.requireNonNull(sqlService);
        this.databaseService = Objects.requireNonNull(databaseService);
        this.commandLineArguments = Objects.requireNonNull(commandLineArguments);
        addOutputProducer(new XmlProducerUsingDOM());
    }

    public SchemaAnalyzer addOutputProducer(OutputProducer outputProducer) {
        outputProducers.add(outputProducer);
        return this;
    }

    public Database analyze(Config config) throws SQLException, IOException {
        // don't render console-based detail unless we're generating HTML (those probably don't have a user watching)
        // and not already logging fine details (to keep from obfuscating those)

        boolean render = config.isHtmlGenerationEnabled();
        ProgressListener progressListener = new ConsoleProgressListener(render, commandLineArguments);

        // if -all(evaluteAll) or -schemas given then analyzeMultipleSchemas
        List<String> schemas = config.getSchemas();
        if (schemas != null || config.isEvaluateAllEnabled()) {
            return this.analyzeMultipleSchemas(config, progressListener);
        } else {
            File outputDirectory = commandLineArguments.getOutputDirectory();
            Objects.requireNonNull(outputDirectory);
            String schema = commandLineArguments.getSchema();
            return analyze(schema, config, outputDirectory, progressListener);
        }
    }

    public Database analyzeMultipleSchemas(Config config, ProgressListener progressListener) throws SQLException, IOException {
        try {
            // following params will be replaced by something appropriate
            List<String> args = config.asList();
            args.remove("-schemas");
            args.remove("-schemata");

            List<String> schemas = config.getSchemas();
            Database db = null;
            String schemaSpec = config.getSchemaSpec();
            Connection connection = getConnection(config);
            DatabaseMetaData meta = connection.getMetaData();
            //-all(evaluteAll) given then get list of the database schemas
            if (schemas == null || config.isEvaluateAllEnabled()) {
                if (schemaSpec == null)
                    schemaSpec = ".*";
                LOGGER.info(
                        "Analyzing schemas that match regular expression '{}'. " +
                        "(use -schemaSpec on command line or in .properties to exclude other schemas)",
                        schemaSpec);
                schemas = DbAnalyzer.getPopulatedSchemas(meta, schemaSpec, false);
                if (schemas.isEmpty())
                    schemas = DbAnalyzer.getPopulatedSchemas(meta, schemaSpec, true);
                if (schemas.isEmpty())
                    schemas.add(config.getUser());
            }

            LOGGER.info("Analyzing schemas: {}{}",
                    System.lineSeparator(),
                    schemas.stream().collect(Collectors.joining(System.lineSeparator())));

            String dbName = config.getDb();
            File outputDir = commandLineArguments.getOutputDirectory();
            // set flag which later on used for generation rootPathtoHome link.
            config.setOneOfMultipleSchemas(true);

            List<MustacheSchema> mustacheSchemas = new ArrayList<>();
            MustacheCatalog mustacheCatalog = null;
            for (String schema : schemas) {
                // reset -all(evaluteAll) and -schemas parameter to avoid infinite loop! now we are analyzing single schema
                config.setSchemas(null);
                config.setEvaluateAllEnabled(false);
                if (dbName == null)
                    config.setDb(schema);
                else
                    config.setSchema(schema);

                LOGGER.info("Analyzing {}", schema);
                File outputDirForSchema = new File(outputDir, schema);
                db = this.analyze(schema, config, outputDirForSchema, progressListener);
                if (db == null) //if any of analysed schema returns null
                    return null;
                mustacheSchemas.add(new MustacheSchema(db.getSchema(), ""));
                mustacheCatalog = new MustacheCatalog(db.getCatalog(), "");
            }

            prepareLayoutFiles(outputDir);
            MustacheCompiler mustacheCompiler = new MustacheCompiler(dbName, config);
            HtmlMultipleSchemasIndexPage htmlMultipleSchemasIndexPage = new HtmlMultipleSchemasIndexPage(mustacheCompiler);
            try (Writer writer = Writers.newPrintWriter(outputDir.toPath().resolve(INDEX_DOT_HTML).toFile())) {
                htmlMultipleSchemasIndexPage.write(mustacheCatalog, mustacheSchemas, config.getDescription(), getDatabaseProduct(meta), writer);
            }
            return db;
        } catch (Config.MissingRequiredParameterException missingParam) {
            config.dumpUsage(missingParam.getMessage(), missingParam.isDbTypeSpecific());
            return null;
        }
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

    public Database analyze(String schema, Config config, File outputDir, ProgressListener progressListener) throws SQLException, IOException {
        try {
            LOGGER.info("Starting schema analysis");

            FileUtils.forceMkdir(outputDir);

            String dbName = config.getDb();

            String catalog = commandLineArguments.getCatalog();

            DatabaseMetaData databaseMetaData = sqlService.connect(config);
            DbmsMeta dbmsMeta = sqlService.getDbmsMeta();

            LOGGER.debug("supportsSchemasInTableDefinitions: {}", databaseMetaData.supportsSchemasInTableDefinitions());
            LOGGER.debug("supportsCatalogsInTableDefinitions: {}", databaseMetaData.supportsCatalogsInTableDefinitions());

            // set default Catalog and Schema of the connection
            catalog = new CatalogResolver(databaseMetaData).resolveCatalog(catalog);
            schema = new SchemaResolver(databaseMetaData).resolveSchema(schema);

            SchemaMeta schemaMeta = config.getMeta() == null ? null : new SchemaMeta(config.getMeta(), dbName, schema);
            if (config.isHtmlGenerationEnabled()) {
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
            databaseService.gatheringSchemaDetails(config, db, schemaMeta, progressListener);


            Collection<Table> tables = new ArrayList<>(db.getTables());
            tables.addAll(db.getViews());

            if (tables.isEmpty()) {
                dumpNoTablesMessage(schema, config.getUser(), databaseMetaData, config.getTableInclusions() != null);
                if (!config.isOneOfMultipleSchemas()) // don't bail if we're doing the whole enchilada
                    throw new EmptySchemaException();
            }

            long duration = progressListener.startedGraphingSummaries();
            if (config.isHtmlGenerationEnabled()) {
                generateHtmlDoc(config, commandLineArguments.useVizJS() , progressListener, outputDir, db, duration, tables);
            }

            outputProducers.forEach(
                    outputProducer -> {
                        try {
                            outputProducer.generate(db, outputDir);
                        } catch (OutputException oe) {
                            if (config.isOneOfMultipleSchemas()) {
                                LOGGER.warn("Failed to produce output", oe);
                            } else {
                                throw oe;
                            }
                        }
                    });

            List<ForeignKeyConstraint> recursiveConstraints = new ArrayList<>();

            // create an orderer to be able to determine insertion and deletion ordering of tables
            TableOrderer orderer = new TableOrderer();

            // side effect is that the RI relationships get trashed
            // also populates the recursiveConstraints collection
            List<Table> orderedTables = orderer.getTablesOrderedByRI(db.getTables(), recursiveConstraints);

            writeOrders(outputDir, orderedTables);

            duration = progressListener.finishedGatheringDetails();
            long overallDuration = progressListener.finished(tables, config);

            if (config.isHtmlGenerationEnabled()) {
                LOGGER.info("Wrote table details in {} seconds", duration / SECONDS_IN_MS);

                LOGGER.info("Wrote relationship details of {} tables/views to directory '{}' in {} seconds.", tables.size(), outputDir, overallDuration / SECONDS_IN_MS);
                LOGGER.info("View the results by opening {}", new File(outputDir, INDEX_DOT_HTML));
            }

            return db;
        } catch (Config.MissingRequiredParameterException missingParam) {
            config.dumpUsage(missingParam.getMessage(), missingParam.isDbTypeSpecific());
            return null;
        }
    }

    private static void writeOrders(File outputDir, List<Table> orderedTables) throws IOException {
        try (PrintWriter out = Writers.newPrintWriter(new File(outputDir, "insertionOrder.txt"))) {
            TextFormatter.getInstance().write(orderedTables, false, out);
        }

        Collections.reverse(orderedTables);
        try (PrintWriter out = Writers.newPrintWriter(new File(outputDir, "deletionOrder.txt"))){
            TextFormatter.getInstance().write(orderedTables, false, out);
        }
    }

    private static void generateHtmlDoc(Config config, boolean useVizJS, ProgressListener progressListener, File outputDir, Database db, long duration, Collection<Table> tables) throws IOException {
        LOGGER.info("Gathered schema details in {} seconds", duration / SECONDS_IN_MS);
        LOGGER.info("Writing/graphing summary");

        prepareLayoutFiles(outputDir);
        DiagramFactory diagramFactory;
        if (useVizJS) {
            diagramFactory = new DiagramFactory(new VizJSDot(),outputDir);
        } else {
            diagramFactory = new DiagramFactory(new GraphvizDot(config),outputDir);
        }
        Path htmlInfoFile = outputDir.toPath().resolve("info-html.txt");
        Files.deleteIfExists(htmlInfoFile);
        writeInfo("date", ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssZ")), htmlInfoFile);
        writeInfo("os", System.getProperty("os.name") + " " + System.getProperty("os.version"), htmlInfoFile);
        writeInfo("schemaspy-version", ManifestUtils.getImplementationVersion(), htmlInfoFile);
        writeInfo("schemaspy-build", ManifestUtils.getImplementationBuild(), htmlInfoFile);
        writeInfo("diagramImplementation", diagramFactory.getImplementationDetails(), htmlInfoFile);
        progressListener.graphingSummaryProgressed();

        boolean showDetailedTables = tables.size() <= config.getMaxDetailedTables();
        final boolean includeImpliedConstraints = config.isImpliedConstraintsEnabled();

        // if evaluating a 'ruby on rails-based' database then connect the columns
        // based on RoR conventions
        // note that this is done before 'hasRealRelationships' gets evaluated so
        // we get a relationships ER diagram
        if (config.isRailsEnabled())
            DbAnalyzer.getRailsConstraints(db.getTablesMap());

        DotFormatter dotProducer = DotFormatter.getInstance();
        MustacheDiagramFactory mustacheDiagramFactory = new MustacheDiagramFactory(diagramFactory);
        ImpliedConstraintsFinder impliedConstraintsFinder = new ImpliedConstraintsFinder();
        MustacheSummaryDiagramFactory mustacheSummaryDiagramFactory = new MustacheSummaryDiagramFactory(dotProducer, mustacheDiagramFactory, impliedConstraintsFinder, outputDir);
        MustacheSummaryDiagramResults results = mustacheSummaryDiagramFactory.generateSummaryDiagrams(db, tables, includeImpliedConstraints, showDetailedTables, progressListener);
        results.getOutputExceptions().stream().forEachOrdered(exception ->
                LOGGER.error("RelationShipDiagramError", exception)
        );
        MustacheCompiler mustacheCompiler = new MustacheCompiler(db.getName(), config);

        HtmlRelationshipsPage htmlRelationshipsPage = new HtmlRelationshipsPage(mustacheCompiler);
        try (Writer writer = Writers.newPrintWriter(outputDir.toPath().resolve("relationships.html").toFile())) {
            htmlRelationshipsPage.write(results, writer);
        }

        progressListener.graphingSummaryProgressed();

        List<Table> orphans = DbAnalyzer.getOrphans(tables);
        MustacheOrphanDiagramFactory mustacheOrphanDiagramFactory = new MustacheOrphanDiagramFactory(dotProducer, mustacheDiagramFactory, outputDir);
        List<MustacheTableDiagram> orphanDiagrams = mustacheOrphanDiagramFactory.generateOrphanDiagrams(orphans);
        HtmlOrphansPage htmlOrphansPage = new HtmlOrphansPage(mustacheCompiler);
        try (Writer writer = Writers.newPrintWriter(outputDir.toPath().resolve("orphans.html").toFile())) {
            htmlOrphansPage.write(orphanDiagrams, orphanDiagrams.size() == orphans.size(), writer);
        }

        progressListener.graphingSummaryProgressed();

        HtmlMainIndexPage htmlMainIndexPage = new HtmlMainIndexPage(mustacheCompiler, config.getDescription());
        try (Writer writer = Writers.newPrintWriter(outputDir.toPath().resolve(INDEX_DOT_HTML).toFile())) {
            htmlMainIndexPage.write(db, tables, results.getImpliedConstraints(), writer);
        }

        progressListener.graphingSummaryProgressed();

        List<ForeignKeyConstraint> constraints = DbAnalyzer.getForeignKeyConstraints(tables);

        HtmlConstraintsPage htmlConstraintsPage = new HtmlConstraintsPage(mustacheCompiler);
        try (Writer writer = Writers.newPrintWriter(outputDir.toPath().resolve("constraints.html").toFile())) {
            htmlConstraintsPage.write(constraints, tables, writer);
        }

        progressListener.graphingSummaryProgressed();

        HtmlAnomaliesPage htmlAnomaliesPage = new HtmlAnomaliesPage(mustacheCompiler);
        try (Writer writer = Writers.newPrintWriter(outputDir.toPath().resolve("anomalies.html").toFile())) {
            htmlAnomaliesPage.write(tables, results.getImpliedConstraints(), writer);
        }

        progressListener.graphingSummaryProgressed();

        HtmlColumnsPage htmlColumnsPage = new HtmlColumnsPage(mustacheCompiler);
        try (Writer writer = Writers.newPrintWriter(outputDir.toPath().resolve("columns.html").toFile())) {
            htmlColumnsPage.write(tables, writer);
        }

        progressListener.graphingSummaryProgressed();

        HtmlRoutinesPage htmlRoutinesPage = new HtmlRoutinesPage(mustacheCompiler);
        try (Writer writer = Writers.newPrintWriter(outputDir.toPath().resolve("routines.html").toFile())) {
            htmlRoutinesPage.write(db.getRoutines(), writer);
        }

        HtmlRoutinePage htmlRoutinePage = new HtmlRoutinePage(mustacheCompiler);
        for (Routine routine : db.getRoutines()) {
            try (Writer writer = Writers.newPrintWriter(outputDir.toPath().resolve("routines").resolve(routine.getName() + DOT_HTML).toFile())) {
                htmlRoutinePage.write(routine, writer);
            }
        }

        HtmlTriggersPage htmlTriggersPage = new HtmlTriggersPage(mustacheCompiler);
        try (Writer writer = Writers.newPrintWriter(outputDir.toPath().resolve("triggers.html").toFile())) {
            htmlTriggersPage.write(db.getTriggers(), writer);
        }

        HtmlTriggerPage htmlTriggerPage = new HtmlTriggerPage(mustacheCompiler);
        for (Trigger trigger : db.getTriggers()) {
            try (Writer writer = Writers.newPrintWriter(outputDir.toPath().resolve("triggers").resolve(trigger.getName() + DOT_HTML).toFile())) {
                htmlTriggerPage.write(trigger, writer);
            }
        }

        // create detailed diagrams

        duration = progressListener.startedGraphingDetails();

        LOGGER.info("Completed summary in {} seconds", duration / SECONDS_IN_MS);
        LOGGER.info("Writing/diagramming details");
        SqlAnalyzer sqlAnalyzer = new SqlAnalyzer(db.getDbmsMeta().getAllKeywords(), db.getTables(), db.getViews());
        MustacheTableDiagramFactory mustacheTableDiagramFactory = new MustacheTableDiagramFactory(dotProducer, mustacheDiagramFactory, outputDir);
        HtmlTablePage htmlTablePage = new HtmlTablePage(mustacheCompiler, sqlAnalyzer);
        for (Table table : tables) {
            List<MustacheTableDiagram> mustacheTableDiagrams = mustacheTableDiagramFactory.generateTableDiagrams(table, results.getStats());
            progressListener.graphingDetailsProgressed(table);
            LOGGER.debug("Writing details of {}", table.getName());
            try (Writer writer = Writers.newPrintWriter(outputDir.toPath().resolve("tables").resolve(table.getName() + DOT_HTML).toFile())) {
                htmlTablePage.write(table, mustacheTableDiagrams, writer);
            }
        }
    }

    /**
     * This method is responsible to copy layout folder to destination directory and not copy template .html files
     *
     * @param outputDir File
     * @throws IOException when not possible to copy layout files to outputDir
     */
    private static void prepareLayoutFiles(File outputDir) throws IOException {
        URL url = null;
        Enumeration<URL> possibleResources = SchemaAnalyzer.class.getClassLoader().getResources("layout");
        while (possibleResources.hasMoreElements() && Objects.isNull(url)) {
            URL possibleResource = possibleResources.nextElement();
            if (!possibleResource.getPath().contains("test-classes")) {
                url = possibleResource;
            }
        }

        IOFileFilter notHtmlFilter = FileFilterUtils.notFileFilter(FileFilterUtils.suffixFileFilter(DOT_HTML));
        FileFilter filter = FileFilterUtils.and(notHtmlFilter);
        ResourceWriter.copyResources(url, outputDir, filter);
    }

    private static void writeInfo(String key, String value, Path infoFile) {
        try {
            Files.write(infoFile, (key + "=" + value + "\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
        } catch (IOException e) {
            LOGGER.error("Failed to write '{}', to '{}'", key + "=" + value, infoFile, e);
        }
    }

    private static Connection getConnection(Config config) throws IOException {
        DbDriverLoader driverLoader = new DbDriverLoader();
        return driverLoader.getConnection(config);
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
                        populatedSchemas.stream().collect(Collectors.joining(System.lineSeparator())));
            }
        }
    }
}
