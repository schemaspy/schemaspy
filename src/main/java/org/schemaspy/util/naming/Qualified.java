package org.schemaspy.util.naming;

/**
 * Qualifies a child with its parent.
 */
public final class Qualified implements Name {

    private final Name child;
    private final Name parent;

    public Qualified(final Name child, final Name parent) {
        this.child = child;
        this.parent = parent;
    }

    @Override
    public String value() {
        if (new EmptyName().equals(this.parent)) {
            return this.child.value();
        } else {
            return this.parent.value() + "." + this.child.value();
        }
    }
}
