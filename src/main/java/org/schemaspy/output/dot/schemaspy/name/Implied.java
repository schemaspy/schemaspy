package org.schemaspy.output.dot.schemaspy.name;

public final class Implied implements Name {

    private final boolean isImplied;
    private final Name origin;

    public Implied(final boolean isImplied, final Name origin) {
        this.isImplied = isImplied;
        this.origin = origin;
    }

    @Override
    public String value() {
        return (this.isImplied ? "Implied" : "") + origin.value();
    }
}
