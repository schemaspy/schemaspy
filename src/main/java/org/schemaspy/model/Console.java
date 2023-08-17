package org.schemaspy.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.Collection;

/**
 * Decorator for rendering console-based details.
 */
public class Console implements ProgressListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final File outputDirectory;
    private final ProgressListener origin;

    public Console(final File outputDirectory, final ProgressListener origin) {
        this.outputDirectory = outputDirectory;
        this.origin = origin;
    }

    @Override
    public void startCollectingTablesViews() {
        origin.startCollectingTablesViews();
        LOGGER.info("Collecting schema information from database");
    }

    @Override
    public void tableViewCollected(Table table) {
        origin.tableViewCollected(table);
        System.out.print('.');
    }

    @Override
    public long finishedCollectingTablesViews() {
        long result = origin.finishedCollectingTablesViews();
        System.out.println();
        System.err.flush();
        System.out.flush();
        LOGGER.info("Collection of schema information finished after {} seconds", result/1000);
        return result;
    }

    @Override
    public void startConnectingTablesViews() {
        origin.startConnectingTablesViews();
        LOGGER.info("Connecting tables and views");
    }

    @Override
    public void connectedTableView(Table table) {
        origin.connectedTableView(table);
        System.out.print('.');
    }

    @Override
    public long finishedConnectingTablesViews() {
        long result = origin.finishedConnectingTablesViews();
        System.out.println();
        System.err.flush();
        System.out.flush();
        LOGGER.info("Tables and views connected after {} seconds", result/1000);
        return result;
    }

    @Override
    public void startCreatingSummaries() {
        origin.startCreatingSummaries();
        LOGGER.info("Writing/graphing summaries");
    }

    @Override
    public void createdSummary() {
        origin.createdSummary();
        System.out.print('.');
    }

    @Override
    public long finishedCreatingSummaries() {
        long result = origin.finishedCreatingSummaries();
        System.out.println();
        System.err.flush();
        System.out.flush();
        LOGGER.info("Summaries created after {} seconds", result/1000);
        return result;
    }

    @Override
    public void startCreatingTablePages() {
        origin.startCreatingTablePages();
        LOGGER.info("Creating table/view pages");
    }

    @Override
    public void createdTablePage(Table table) {
        origin.createdTablePage(table);
        System.out.print('.');
    }

    @Override
    public long finishedCreatingTablePages() {
        long result = origin.finishedCreatingTablePages();
        System.out.println();
        System.err.flush();
        System.out.flush();
        LOGGER.info("Created table/view pages after {} seconds", result/1000);
        return result;
    }

    @Override
    public long finished(Collection<Table> tables) {
        long result = origin.finished(tables);
        System.err.flush();
        System.out.flush();
        LOGGER.info("Wrote relationship details of {} tables/view to directory '{}' in {} seconds", tables.size(), outputDirectory, result/1000);
        return result;
    }
}
