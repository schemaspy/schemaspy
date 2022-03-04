package org.schemaspy.output.dot.schemaspy.name;

/**
 * Decorates a name based on compactness.
 */
public final class Sized implements Name {

    private final boolean isCompact;
    private final Name origin;

    /**
     * Constructor.
     *
     * @param isCompact True if the graph is compact, false if large.
     * @param origin The name to be decorated.
     */
    public Sized(final boolean isCompact, final Name origin) {
        this.isCompact = isCompact;
        this.origin = origin;
    }

    @Override
    public String value() {
        return (isCompact ? "compact" : "large") + origin.value();
    }
}
