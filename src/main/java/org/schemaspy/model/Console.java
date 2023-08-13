package org.schemaspy.model;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.atomic.LongAdder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decorator for rendering console-based details.
 */
public class Console implements ProgressListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final String output;
    private final LongAdder tableDetails = new LongAdder();
    private final LongAdder connectingProgress = new LongAdder();
    private final LongAdder tablePages = new LongAdder();
    private final long startedAt;
    private long startedGatheringSchemaDetailsAt;
    private long startedConnectingTablesAt;
    private long startedGraphingSummariesAt;
    private long startedGraphingTablesAt;

    public Console(final String output) {
        this.output = output;
        this.startedAt = System.currentTimeMillis();
    }

    @Override
    public void startCollectingTablesViews() {
        this.startedGatheringSchemaDetailsAt = System.currentTimeMillis();
        LOGGER.info("Gathering schema details");
    }

    @Override
    public void tableViewCollected(Table table) {
        tableDetails.increment();
        if (tableDetails.sum() % 10 == 0) {
            LOGGER.info("Found {} tables/views...", tableDetails.sum());
        }
    }

    @Override
    public void finishedCollectingTablesViews() {
        final long duration = System.currentTimeMillis() - this.startedGatheringSchemaDetailsAt;
        LOGGER.info("Found total of {} tables/views in {} seconds", tableDetails.sum(), duration/1000);
    }

    @Override
    public void startedConnectingTablesViews() {
        this.startedConnectingTablesAt = System.currentTimeMillis();
        LOGGER.info("Connecting relationships");
    }

    @Override
    public void connectedTableView(Table table) {
        connectingProgress.increment();
        if (connectingProgress.sum() % 10 == 0){
            LOGGER.info("Connected {} tables/views...", connectingProgress.sum());
        }
    }

    @Override
    public void finishedConnectingTablesViews() {
        final long duration = System.currentTimeMillis() - this.startedConnectingTablesAt;
        LOGGER.info("Connected a total of {} tables/views in {} seconds", connectingProgress.sum(), duration/1000);
    }

    @Override
    public void startCreatingSummaries() {
        this.startedGraphingSummariesAt = System.currentTimeMillis();
        LOGGER.info("Writing/graphing summary");
    }

    @Override
    public void createdSummary(String summary) {
        LOGGER.info("Created summary diagram {}", summary);
    }

    @Override
    public void finishedCreatingSummaries() {
        final long duration = System.currentTimeMillis() - this.startedGraphingSummariesAt;
        LOGGER.info("Summary completed in {} seconds", duration / 1000);
    }

    @Override
    public void startCreatingTablePages() {
        this.startedGraphingTablesAt = System.currentTimeMillis();
        LOGGER.info("Writing table/view pages");
    }

    @Override
    public void createdTablePage(Table table) {
        tablePages.increment();
        if (tablePages.sum() % 10 == 0) {
            LOGGER.info("Written {} table/view pages...", tablePages.sum());
        }
    }

    @Override
    public void finishedCreatingTablePages() {
        long result = System.currentTimeMillis() - startedGraphingTablesAt;
        LOGGER.info("Wrote {} table/view pages in {} seconds", tablePages.sum(), result / 1000);
    }

    @Override
    public void finished(Database database) {
        long duration = System.currentTimeMillis() - startedAt;
        LOGGER.info(
            "Finished documenting '{}' in {} after {} seconds",
            database.getCatalog().getName() + "." + database.getSchema().getName(),
            output,
            duration / 1000
        );
    }
}
