package org.schemaspy.output.dot.schemaspy.name;

public final class Implied implements Name {

    private final boolean isImplied;

    public Implied(final boolean isImplied) {
        this.isImplied = isImplied;
    }

    @Override
    public String value() {
        return this.isImplied ? "Implied" : "";
    }
}
