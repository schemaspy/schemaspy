package org.schemaspy.output.dot.schemaspy.name;

/**
 * Encapsulates a name based on compactness.
 */
public final class Sized implements Name {

    private final boolean isCompact;

    /**
     * Constructor.
     *
     * @param isCompact True if the graph is compact, false if large.
     */
    public Sized(final boolean isCompact) {
        this.isCompact = isCompact;
    }

    @Override
    public String value() {
        return this.isCompact ? "compact" : "large";
    }
}
