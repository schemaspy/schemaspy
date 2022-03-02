package org.schemaspy.output.dot.schemaspy.name;

/**
 * Decorates a name with the default name.
 */
public final class DefaultName implements Name {

    private final Name origin;

    public DefaultName(final Name origin) {
        this.origin = origin;
    }

    @Override
    public String value() {
        return "RelationshipsDiagram" + origin.value();
    }
}
