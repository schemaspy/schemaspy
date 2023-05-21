package org.schemaspy.input.dbms.driverpath;

import java.util.Objects;

/**
 * Uses the fallback driverpath in case the first is unavailable.
 */
public final class DpFallback implements Driverpath {

    private final Driverpath primary;
    private final Driverpath secondary;

    public DpFallback(final Driverpath primary, final Driverpath secondary) {
        this.primary = primary;
        this.secondary = secondary;
    }

    @Override
    public String value() {
        final String value = this.primary.value();
        return Objects.nonNull(value) ? value : this.secondary.value();
    }
}
