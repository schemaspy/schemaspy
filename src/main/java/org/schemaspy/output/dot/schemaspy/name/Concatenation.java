package org.schemaspy.output.dot.schemaspy.name;

/**
 * Joins two names.
 */
public final class Concatenation implements Name {

    private final Name first;
    private final Name second;

    /**
     * Constructs a decorator that appends one name to the other.
     * @param first The name to append to.
     * @param second The name to append.
     */
    public Concatenation(final Name first, final Name second) {
        this.first = first;
        this.second = second;
    }
    @Override
    public String value() {
        return this.first.value() + this.second.value();
    }
}
