package org.schemaspy.output.dot.schemaspy.name;

/**
 * Encapsulates what name to use based on number of degrees of separation.
 */
public final class Degree implements Name {

    private final boolean twoDegreesOfSeparation;

    /**
     * Constructor.
     *
     * @param twoDegreesOfSeparation True if the degrees of separation is two,
           false if that number is one.
     */
    public Degree(final boolean twoDegreesOfSeparation) {
        this.twoDegreesOfSeparation = twoDegreesOfSeparation;
    }

    @Override
    public String value() {
        return twoDegreesOfSeparation ? "twoDegrees" : "oneDegree";
    }
}
