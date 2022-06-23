package org.schemaspy.model;

import org.schemaspy.Config;
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
    public long startedGatheringDetails() {
        long result = origin.startedGatheringDetails();
        System.out.print("Gathering schema details...");
        return result;
    }

    @Override
    public void gatheringDetailsProgressed(Table table) {
        origin.gatheringDetailsProgressed(table);
        System.out.print('.');
    }

    @Override
    public long startedConnectingTables() {
        long result = origin.startedConnectingTables();
        System.err.flush();
        System.out.flush();
        System.out.println("(" + result / 1000 + "sec)");
        System.out.print("Connecting relationships...");
        return result;
    }

    @Override
    public void connectingTablesProgressed(Table table) {
        origin.connectingTablesProgressed(table);
        System.out.print('.');
    }

    @Override
    public long startedGraphingSummaries() {
        long result = origin.startedGraphingSummaries();
        System.err.flush();
        System.out.flush();
        System.out.println("(" + result / 1000 + "sec)");
        System.out.print("Writing/graphing summary");
        System.out.print('.');
        return result;

    }

    @Override
    public void graphingSummaryProgressed() {
        origin.graphingSummaryProgressed();
        System.out.print('.');
    }

    @Override
    public long startedGraphingDetails() {
        long result = origin.startedGraphingDetails();
        System.err.flush();
        System.out.flush();
        System.out.println("(" + result / 1000 + "sec)");
        System.out.print("Writing/diagramming details");
        return result;
    }

    @Override
    public void graphingDetailsProgressed(Table table) {
        origin.graphingDetailsProgressed(table);
        System.out.print('.');
    }

    @Override
    public long finishedGatheringDetails() {
        long result = origin.finishedGatheringDetails();
        System.err.flush();
        System.out.flush();
        System.out.println("(" + result / 1000 + "sec)");
        return result;
    }

    @Override
    public long finished(Collection<Table> tables, Config config) {
        long result = origin.finished(tables, config);
        System.err.flush();
        System.out.flush();
        System.out.println("Wrote relationship details of " + tables.size() + " tables/views to directory '" + commandLineArguments.getOutputDirectory() + "' in " + result / 1000 + " seconds.");
        System.out.println("View the results by opening " + new File(commandLineArguments.getOutputDirectory(), "index.html"));
        return result;
    }
}
