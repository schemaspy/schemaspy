package org.schemaspy.util.naming;

/**
 * Null object for name.
 */
public final class EmptyName implements Name {

    public EmptyName() {
        // Null object is constant.
    }

    @Override
    public String value() {
        return "";
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Name)) {
            return false;
        }
        Name other = (Name) obj;
        return other.value().equals(this.value());
    }
}
