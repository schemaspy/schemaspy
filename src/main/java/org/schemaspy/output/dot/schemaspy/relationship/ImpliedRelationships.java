package org.schemaspy.output.dot.schemaspy.relationship;

import java.io.PrintWriter;
import java.util.Set;
import org.schemaspy.model.ForeignKeyConstraint;
import org.schemaspy.model.Table;
import org.schemaspy.output.dot.DotConfig;
import org.schemaspy.output.dot.schemaspy.DotFormat;
import org.schemaspy.output.dot.schemaspy.DotTableFormatter;
import org.schemaspy.view.WriteStats;

/**
 * Represents implied relationships associated with a table.
 */
public final class ImpliedRelationships implements Relationships {

    private final Relationships origin;

    public ImpliedRelationships(
        final DotFormat dotFormat,
        final DotConfig dotConfig,
        final Table table,
        final boolean twoDegreesOfSeparation,
        final WriteStats stats,
        final PrintWriter dot
    ) {
        this(
            new DotTableFormatter(
                dotFormat,
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
    public Set<ForeignKeyConstraint> write() {
        return origin.write();
    }
}
