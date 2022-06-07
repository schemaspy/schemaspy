package org.schemaspy.output.dot.schemaspy.relationship;

import org.schemaspy.model.Table;
import org.schemaspy.output.dot.DotConfig;
import org.schemaspy.output.dot.schemaspy.DotTableFormatter;

import java.io.PrintWriter;
import java.util.concurrent.atomic.LongAdder;

/**
 * Represents implied relationships associated with a table.
 */
public final class ImpliedRelationships implements Relationships {

    private final Relationships origin;

    public ImpliedRelationships(
        final DotConfig dotConfig,
        final Table table,
        final boolean twoDegreesOfSeparation,
        final LongAdder stats,
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
