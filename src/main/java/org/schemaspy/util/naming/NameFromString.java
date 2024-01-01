package org.schemaspy.util.naming;

public class NameFromString implements Name {

    private final String value;

    public NameFromString(String value) {
        this.value = value;
    }

    @Override
    public String value() {
        return value;
    }
}
