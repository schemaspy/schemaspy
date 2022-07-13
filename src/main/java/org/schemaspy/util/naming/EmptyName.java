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
}
