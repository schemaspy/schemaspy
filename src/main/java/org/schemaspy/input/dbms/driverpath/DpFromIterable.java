package org.schemaspy.input.dbms.driverpath;

import java.io.File;
import java.nio.file.Path;

import org.schemaspy.input.dbms.DbDriverLoader;
import org.schemaspy.util.Filtered;
import org.schemaspy.util.Joined;
import org.schemaspy.util.Mapped;
import org.schemaspy.util.WhenFalse;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 * Micro type for Driver path
 */
public final class DpFromIterable implements Driverpath {

    private final Iterable<Path> driverPath;

    public DpFromIterable(final Iterable<Path> driverPath) {
        this.driverPath =
          new Joined<>(
            new Mapped<>(
              new Filtered<>(
                driverPath,
                new WhenFalse<>(
                  new PathExist(),
                  new LogPath(
                    "Provided -dp(driverPath) '{}' doesn't exist",
                    LoggerFactory.getLogger(DbDriverLoader.class),
                    Level.WARN
                  )
                )
              ),
              SubPaths::new
            )
          );
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
