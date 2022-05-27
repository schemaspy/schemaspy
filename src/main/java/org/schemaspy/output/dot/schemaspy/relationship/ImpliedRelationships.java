package org.schemaspy.output.dot.schemaspy.relationship;

import org.schemaspy.model.Table;
import org.schemaspy.output.dot.DotConfig;
import org.schemaspy.output.dot.schemaspy.DotTableFormatter;
import org.schemaspy.view.WriteStats;

import java.io.PrintWriter;

/**
 * Represents implied relationships associated with a table.
 */
public final class ImpliedRelationships implements Relationships {

    private final Relationships origin;

    public ImpliedRelationships(
        final DotConfig dotConfig,
        final Table table,
        final boolean twoDegreesOfSeparation,
        final WriteStats stats,
        final PrintWriter dot
    ) {
        this(
            new DotTableFormatter(
                dotConfig,
                table,
                twoDegreesOfSeparation,
                stats,
                true,
                dot)
        );
    }

    public ImpliedRelationships(final Relationships origin) {
        this.origin = origin;
    }

    @Override
    public void write() {
        origin.write();
    }
}
