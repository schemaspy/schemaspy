package org.schemaspy.input.dbms.driverpath;

import java.io.File;
import java.nio.file.Path;

import org.schemaspy.util.Mapped;

/**
 * Micro type for Driver path
 */
public final class DpFromIterable implements Driverpath {

    private final Iterable<Path> driverPath;

    public DpFromIterable(final Iterable<Path> driverPath) {
        this.driverPath = driverPath;
    }

    @Override
    public String value() {
        return String.join(
            File.pathSeparator,
            new Mapped<>(
                this.driverPath,
                Path::toString
            )
        );
    }
}
