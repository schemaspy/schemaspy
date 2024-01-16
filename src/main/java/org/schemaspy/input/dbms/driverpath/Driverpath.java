package org.schemaspy.input.dbms.driverpath;

import java.io.File;
import java.nio.file.Path;

import org.schemaspy.util.Mapped;

/**
 * Abstracts a location to a JDBC driver.
 */
public interface Driverpath extends Iterable<Path> {

    /**
     * Asks the driver path to represent itself in text.
     * @return A textual representation of the JDBC driver location.
     */
    default String value() {
        return String.join(
          File.pathSeparator,
          new Mapped<>(
            this,
            Path::toString
          )
        );
    }
}
