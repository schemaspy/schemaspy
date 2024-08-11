package org.schemaspy;

import org.schemaspy.model.Table;
import org.schemaspy.util.DefaultPrintWriter;
import org.schemaspy.view.TextFormatter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

public class OrderingReport {

    private final File outputDir;
    private final List<Table> tables;

    public OrderingReport(final File outputDir, final List<Table> orderedTables) {
        this.outputDir = outputDir;
        this.tables = orderedTables;
    }

    public void write() throws IOException {
        try (PrintWriter out = new DefaultPrintWriter(new File(outputDir, "insertionOrder.txt"))) {
            new TextFormatter(tables, false, out).write();
        }

        Collections.reverse(tables);
        try (PrintWriter out = new DefaultPrintWriter(new File(outputDir, "deletionOrder.txt"))){
            new TextFormatter(tables, false, out).write();
        }
    }
}
