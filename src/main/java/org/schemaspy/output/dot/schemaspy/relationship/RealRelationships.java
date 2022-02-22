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
 * Represents real relationships (excluding implied) associated with a table.
 */
public final class RealRelationships implements Relationships {

    private final Relationships origin;

    public RealRelationships(
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
                false,
                dot)
        );
    }

    public RealRelationships(final Relationships origin) {
        this.origin = origin;
    }

    @Override
    public Set<ForeignKeyConstraint> write() {
        return origin.write();
    }
}
