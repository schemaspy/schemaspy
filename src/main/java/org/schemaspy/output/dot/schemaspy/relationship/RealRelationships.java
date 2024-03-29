package org.schemaspy.output.dot.schemaspy.relationship;

import org.schemaspy.model.Table;
import org.schemaspy.output.dot.RuntimeDotConfig;
import org.schemaspy.output.dot.schemaspy.DotTableFormatter;

import java.io.PrintWriter;
import java.util.concurrent.atomic.LongAdder;

/**
 * Represents real relationships (excluding implied) associated with a table.
 */
public final class RealRelationships implements Relationships {

    private final Relationships origin;

    public RealRelationships(
        final RuntimeDotConfig runtimeDotConfig,
        final Table table,
        final boolean twoDegreesOfSeparation,
        final LongAdder stats,
        final PrintWriter dot
    ) {
        this(
            new DotTableFormatter(
                runtimeDotConfig,
                table,
                twoDegreesOfSeparation,
                stats,
                false,
                dot)
        );
    }

    public RealRelationships(final Relationships origin) {
        this.origin = origin;
    }

    @Override
    public void write() {
        origin.write();
    }
}
