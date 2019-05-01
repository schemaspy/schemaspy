package org.schemaspy.model;

public class Sequence implements Comparable<Sequence> {
    private final String name;
    private final Integer startValue;
    private final Integer increment;

    public Sequence(String name, Integer startValue, Integer increment) {
        this.name = name;
        this.startValue = startValue;
        this.increment = increment;
    }

    public String getName() {
        return name;
    }

    public Integer getStartValue() {
        return startValue;
    }

    public Integer getIncrement() {
        return increment;
    }

    @Override
    public int compareTo(Sequence other) {
        int rc = getName().compareTo(other.getName());
        if (rc == 0)
            rc = getStartValue().compareTo(other.getStartValue());
        if (rc == 0)
            rc = getIncrement().compareTo(other.getIncrement());
        return rc;
    }
}
