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
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.model.*;
import org.schemaspy.model.xml.SchemaMeta;
import org.schemaspy.output.OutputException;
import org.schemaspy.output.OutputProducer;
import org.schemaspy.output.xml.dom.XmlProducerUsingDOM;
import org.schemaspy.service.DatabaseService;
import org.schemaspy.service.SqlService;
import org.schemaspy.util.Dot;
import org.schemaspy.util.LineWriter;
import org.schemaspy.util.ResourceWriter;
import org.schemaspy.view.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
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
            Connection connection = this.getConnection(config);
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

            LOGGER.info("Analyzing schemas: " + System.lineSeparator() + "{}",
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
            HtmlMultipleSchemasIndexPage.getInstance().write(outputDir, dbName, mustacheCatalog, mustacheSchemas, meta);

            return db;
        } catch (Config.MissingRequiredParameterException missingParam) {
            config.dumpUsage(missingParam.getMessage(), missingParam.isDbTypeSpecific());
            return null;
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
            if (schema == null)
                schema = databaseMetaData.getConnection().getSchema();
            if (catalog == null)
                catalog = databaseMetaData.getConnection().getCatalog();

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

            long duration = progressListener.startedGraphingSummaries();

            Collection<Table> tables = new ArrayList<>(db.getTables());
            tables.addAll(db.getViews());

            if (tables.isEmpty()) {
                dumpNoTablesMessage(schema, config.getUser(), databaseMetaData, config.getTableInclusions() != null);
                if (!config.isOneOfMultipleSchemas()) // don't bail if we're doing the whole enchilada
                    throw new EmptySchemaException();
            }

            if (config.isHtmlGenerationEnabled()) {
                generateHtmlDoc(config, progressListener, outputDir, db, duration, tables);
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
                LOGGER.info("Wrote table details in {} seconds", duration / 1000);

                LOGGER.info("Wrote relationship details of {} tables/views to directory '{}' in {} seconds.", tables.size(), outputDir, overallDuration / 1000);
                LOGGER.info("View the results by opening {}", new File(outputDir, "index.html"));
            }

            return db;
        } catch (Config.MissingRequiredParameterException missingParam) {
            config.dumpUsage(missingParam.getMessage(), missingParam.isDbTypeSpecific());
            return null;
        }
    }

    private void writeOrders(File outputDir, List<Table> orderedTables) throws IOException {
        LineWriter out;
        out = new LineWriter(new File(outputDir, "insertionOrder.txt"), 16 * 1024, Config.DOT_CHARSET);
        try {
            TextFormatter.getInstance().write(orderedTables, false, out);
        } catch (IOException e) {
            throw new IOException(e);
        } finally {
            out.close();
        }

        out = new LineWriter(new File(outputDir, "deletionOrder.txt"), 16 * 1024, Config.DOT_CHARSET);
        try {
            Collections.reverse(orderedTables);
            TextFormatter.getInstance().write(orderedTables, false, out);
        } catch (IOException e) {
            throw new IOException(e);
        } finally {
            out.close();
        }
    }

    private void generateHtmlDoc(Config config, ProgressListener progressListener, File outputDir, Database db, long duration, Collection<Table> tables) throws IOException {
        LineWriter out;
        LOGGER.info("Gathered schema details in {} seconds", duration / 1000);
        LOGGER.info("Writing/graphing summary");

        prepareLayoutFiles(outputDir);

        progressListener.graphingSummaryProgressed();

        boolean showDetailedTables = tables.size() <= config.getMaxDetailedTables();
        final boolean includeImpliedConstraints = config.isImpliedConstraintsEnabled();

        // if evaluating a 'ruby on rails-based' database then connect the columns
        // based on RoR conventions
        // note that this is done before 'hasRealRelationships' gets evaluated so
        // we get a relationships ER diagram
        if (config.isRailsEnabled())
            DbAnalyzer.getRailsConstraints(db.getTablesMap());

        File summaryDir = new File(outputDir, "diagrams/summary");

        // generate the compact form of the relationships .dot file
        String dotBaseFilespec = "relationships";
        out = new LineWriter(new File(summaryDir, dotBaseFilespec + ".real.compact.dot"), Config.DOT_CHARSET);
        WriteStats stats = new WriteStats(tables);
        DotFormatter.getInstance().writeRealRelationships(db, tables, true, showDetailedTables, stats, out, outputDir);
        boolean hasRealRelationships = stats.getNumTablesWritten() > 0 || stats.getNumViewsWritten() > 0;
        out.close();

        if (hasRealRelationships) {
            // real relationships exist so generate the 'big' form of the relationships .dot file
            progressListener.graphingSummaryProgressed();
            out = new LineWriter(new File(summaryDir, dotBaseFilespec + ".real.large.dot"), Config.DOT_CHARSET);
            DotFormatter.getInstance().writeRealRelationships(db, tables, false, showDetailedTables, stats, out, outputDir);
            out.close();
        }

        // getting implied constraints has a side-effect of associating the parent/child tables, so don't do it
        // here unless they want that behavior
        List<ImpliedForeignKeyConstraint> impliedConstraints = new ArrayList();
        if (includeImpliedConstraints)
            impliedConstraints.addAll(DbAnalyzer.getImpliedConstraints(tables));

        List<Table> orphans = DbAnalyzer.getOrphans(tables);
        config.setHasOrphans(!orphans.isEmpty() && Dot.getInstance().isValid());
        config.setHasRoutines(!db.getRoutines().isEmpty());

        progressListener.graphingSummaryProgressed();

        File impliedDotFile = new File(summaryDir, dotBaseFilespec + ".implied.compact.dot");
        out = new LineWriter(impliedDotFile, Config.DOT_CHARSET);
        boolean hasImplied = DotFormatter.getInstance().writeAllRelationships(db, tables, true, showDetailedTables, stats, out, outputDir);

        Set<TableColumn> excludedColumns = stats.getExcludedColumns();
        out.close();
        if (hasImplied) {
            impliedDotFile = new File(summaryDir, dotBaseFilespec + ".implied.large.dot");
            out = new LineWriter(impliedDotFile, Config.DOT_CHARSET);
            DotFormatter.getInstance().writeAllRelationships(db, tables, false, showDetailedTables, stats, out, outputDir);
            out.close();
        } else {
            Files.deleteIfExists(impliedDotFile.toPath());
        }

        HtmlRelationshipsPage.getInstance().write(db, summaryDir, dotBaseFilespec, hasRealRelationships, hasImplied, excludedColumns,
                progressListener, outputDir);

        progressListener.graphingSummaryProgressed();

        File orphansDir = new File(outputDir, "diagrams/orphans");
        FileUtils.forceMkdir(orphansDir);
        HtmlOrphansPage.getInstance().write(db, orphans, orphansDir, outputDir);
        out.close();

        progressListener.graphingSummaryProgressed();

        HtmlMainIndexPage.getInstance().write(db, tables, impliedConstraints, outputDir);

        progressListener.graphingSummaryProgressed();

        List<ForeignKeyConstraint> constraints = DbAnalyzer.getForeignKeyConstraints(tables);
        HtmlConstraintsPage constraintIndexFormatter = HtmlConstraintsPage.getInstance();
        constraintIndexFormatter.write(db, constraints, tables, outputDir);

        progressListener.graphingSummaryProgressed();

        HtmlAnomaliesPage.getInstance().write(db, tables, impliedConstraints, outputDir);

        progressListener.graphingSummaryProgressed();

        HtmlColumnsPage.getInstance().write(db, tables, outputDir);

        progressListener.graphingSummaryProgressed();

        HtmlRoutinesPage.getInstance().write(db, outputDir);

        // create detailed diagrams

        duration = progressListener.startedGraphingDetails();

        LOGGER.info("Completed summary in {} seconds", duration / 1000);
        LOGGER.info("Writing/diagramming details");

        generateTables(progressListener, outputDir, db, tables, stats);
    }

    /**
     * This method is responsible to copy layout folder to destination directory and not copy template .html files
     *
     * @param outputDir File
     * @throws IOException when not possible to copy layout files to outputDir
     */
    private void prepareLayoutFiles(File outputDir) throws IOException {
        URL url = null;
        Enumeration<URL> possibleResources = getClass().getClassLoader().getResources("layout");
        while (possibleResources.hasMoreElements() && Objects.isNull(url)) {
            URL possibleResource = possibleResources.nextElement();
            if (!possibleResource.getPath().contains("test-classes")) {
                url = possibleResource;
            }
        }

        IOFileFilter notHtmlFilter = FileFilterUtils.notFileFilter(FileFilterUtils.suffixFileFilter(".html"));
        FileFilter filter = FileFilterUtils.and(notHtmlFilter);
        ResourceWriter.copyResources(url, outputDir, filter);
    }

    private Connection getConnection(Config config) throws IOException {
        DbDriverLoader driverLoader = new DbDriverLoader();
        return driverLoader.getConnection(config);
    }

    private void generateTables(ProgressListener progressListener, File outputDir, Database db, Collection<Table> tables, WriteStats stats) throws IOException {
        HtmlTablePage htmlTablePage = new HtmlTablePage(db);
        for (Table table : tables) {
            progressListener.graphingDetailsProgressed(table);
            LOGGER.debug("Writing details for {}", table.getName());

            htmlTablePage.write(db, table, outputDir, stats);
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
            return;
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
                    "Available schemas(Some of these may be user or system schemas):" +
                    System.lineSeparator() + "{}",
                    schemas.stream().collect(Collectors.joining(System.lineSeparator())));
            List<String> populatedSchemas = DbAnalyzer.getPopulatedSchemas(meta);
            if (populatedSchemas.isEmpty()) {
                LOGGER.error("Unable to determine if any of the schemas contain tables/views");
            } else {
                LOGGER.info("Schemas with tables/views visible to '{}':" + System.lineSeparator() + "{}",
                        populatedSchemas.stream().collect(Collectors.joining(System.lineSeparator())));
            }
        }
    }
}
