package org.schemaspy.output.dot.schemaspy.name;

/**
 * Decorates a name based on number of degrees of separation.
 */
public final class Degree implements Name {

    private final boolean twoDegreesOfSeparation;
    private final Name origin;

    /**
     * Constructor.
     *
     * @param twoDegreesOfSeparation True if the degrees of separation is two,
           false if that number is one.
     * @param origin The name to be decorated.
     */
    public Degree(final boolean twoDegreesOfSeparation, final Name origin) {
        this.twoDegreesOfSeparation = twoDegreesOfSeparation;
        this.origin = origin;
    }

    @Override
    public String value() {
        return (twoDegreesOfSeparation ? "twoDegrees" : "oneDegree") + origin.value();
    }
}
