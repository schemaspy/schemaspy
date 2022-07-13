package org.schemaspy.output.dot.schemaspy.name;

import org.schemaspy.util.naming.Name;

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
