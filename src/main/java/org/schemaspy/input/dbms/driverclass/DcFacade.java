package org.schemaspy.input.dbms.driverclass;

import org.schemaspy.input.dbms.classloader.ClassloaderSource;
import org.schemaspy.input.dbms.exceptions.ConnectionFailure;

import java.sql.Driver;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Simplifies the logic for obtaining a JDBC driver's class.
 */
public class DcFacade implements Driverclass {
    private final String[] driverClasses;
    private final ClassloaderSource loader;

    public DcFacade(final String[] driverClasses, final ClassloaderSource loader) {
        this.driverClasses = driverClasses;
        this.loader = loader;
    }

    @Override
    public Class<Driver> value() {
        List<Driverclass> candidates = Arrays.stream(this.driverClasses)
                .map(candidate -> new DcErrorLogged(new DcClassloader(candidate, this.loader.classloader())))
                .collect(Collectors.toList());
        try {
            return new DcIterator(candidates.iterator()).value();
        } catch (NoSuchElementException e) {
            throw new ConnectionFailure(
                String.format(
                    "Failed to create any of '%s' driver from driver path.", String.join(", ", this.driverClasses)
                )
            );
        }
    }
}
