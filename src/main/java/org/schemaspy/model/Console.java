package org.schemaspy.model;

import org.schemaspy.cli.CommandLineArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Collection;

/**
 * Decorator for rendering console-based details.
 */
public class Console implements ProgressListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final CommandLineArguments commandLineArguments;
    private final ProgressListener origin;

    public Console(final CommandLineArguments commandLineArguments, final ProgressListener origin) {
        this.commandLineArguments = commandLineArguments;
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
    public long startConnectingTablesViews() {
        long result = origin.startConnectingTablesViews();
        System.out.println();
        System.err.flush();
        System.out.flush();
        LOGGER.info("Collection of schema information finished after {} seconds", result/1000);
        LOGGER.info("Connecting tables and views");
        return result;
    }

    @Override
    public void connectedTableView(Table table) {
        origin.connectedTableView(table);
        System.out.print('.');
    }

    @Override
    public long startCreatingSummaries() {
        long result = origin.startCreatingSummaries();
        System.out.println();
        System.err.flush();
        System.out.flush();
        LOGGER.info("Tables and views connected after {} seconds", result/1000);
        LOGGER.info("Writing/graphing summaries");
        return result;

    }

    @Override
    public void createdSummary() {
        origin.createdSummary();
        System.out.print('.');
    }

    @Override
    public long startCreatingTablePages() {
        long result = origin.startCreatingTablePages();
        System.out.println();
        System.err.flush();
        System.out.flush();
        LOGGER.info("Summaries created after {} seconds", result/1000);
        LOGGER.info("Creating table/view pages");
        return result;
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
        LOGGER.info("Wrote relationship details of {} tables/view to directory '{}' in {} seconds", tables.size(), commandLineArguments.getOutputDirectory(), result/1000);
        return result;
    }
}
