package org.schemaspy.util.naming;

import java.util.Objects;

public final class NameFromString implements Name {

    private final String value;

    public NameFromString(final String value) {
        this.value = value;
    }

    @Override
    public String value() {
        return !Objects.isNull(this.value)
            ? this.value
            : new EmptyName().value();
    }
}
