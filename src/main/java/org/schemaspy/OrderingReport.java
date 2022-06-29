package org.schemaspy;

import org.schemaspy.model.Table;
import org.schemaspy.util.Writers;
import org.schemaspy.view.TextFormatter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

public class OrderingReport {

    public static void write(File outputDir, List<Table> orderedTables) throws IOException {
        try (PrintWriter out = Writers.newPrintWriter(new File(outputDir, "insertionOrder.txt"))) {
            TextFormatter.getInstance().write(orderedTables, false, out);
        }

        Collections.reverse(orderedTables);
        try (PrintWriter out = Writers.newPrintWriter(new File(outputDir, "deletionOrder.txt"))){
            TextFormatter.getInstance().write(orderedTables, false, out);
        }
    }
}
