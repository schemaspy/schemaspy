package org.schemaspy.model;

import org.schemaspy.cli.CommandLineArguments;

import java.io.File;
import java.util.Collection;

/**
 * Decorator for rendering console-based details.
 */
public class Console implements ProgressListener {

    private final CommandLineArguments commandLineArguments;
    private final ProgressListener origin;

    public Console(final CommandLineArguments commandLineArguments, final ProgressListener origin) {
        this.commandLineArguments = commandLineArguments;
        this.origin = origin;
    }

    @Override
    public void startCollectingTablesViews() {
        origin.startCollectingTablesViews();
        System.out.print("Gathering schema details...");
    }

    @Override
    public void tableViewCollected(Table table) {
        origin.tableViewCollected(table);
        System.out.print('.');
    }

    @Override
    public long startConnectingTablesViews() {
        long result = origin.startConnectingTablesViews();
        System.err.flush();
        System.out.flush();
        System.out.println("(" + result / 1000 + "sec)");
        System.out.print("Connecting relationships...");
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
        System.err.flush();
        System.out.flush();
        System.out.println("(" + result / 1000 + "sec)");
        System.out.print("Writing/graphing summary");
        System.out.print('.');
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
        System.err.flush();
        System.out.flush();
        System.out.println("(" + result / 1000 + "sec)");
        System.out.print("Writing/diagramming details");
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
        System.err.flush();
        System.out.flush();
        System.out.println("(" + result / 1000 + "sec)");
        return result;
    }

    @Override
    public long finished(Collection<Table> tables) {
        long result = origin.finished(tables);
        System.err.flush();
        System.out.flush();
        System.out.println("Wrote relationship details of " + tables.size() + " tables/views to directory '" + commandLineArguments.getOutputDirectory() + "' in " + result / 1000 + " seconds.");
        System.out.println("View the results by opening " + new File(commandLineArguments.getOutputDirectory(), "index.html"));
        return result;
    }
}
