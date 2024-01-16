package org.schemaspy.input.dbms.driverclass;

import java.lang.invoke.MethodHandles;
import java.sql.Driver;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.schemaspy.input.dbms.classloader.ClSticky;
import org.schemaspy.input.dbms.classloader.ClassloaderSource;
import org.schemaspy.input.dbms.exceptions.ConnectionFailure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simplifies the logic for obtaining a JDBC driver's class.
 */
public class DcFacade implements Driverclass {
    private final Logger logger;
    private final String[] driverClasses;
    private final ClassloaderSource loader;

    public DcFacade(final String[] driverClasses, final ClassloaderSource loader) {
        this(LoggerFactory.getLogger(MethodHandles.lookup().lookupClass()), driverClasses, loader);
    }

    public DcFacade(final Logger logger, final String[] driverClasses, final ClassloaderSource loader) {
        this.logger = logger;
        this.driverClasses = driverClasses;
        this.loader = new ClSticky(loader);
    }

    @Override
    public Class<Driver> value() {
        List<Driverclass> candidates = Arrays.stream(this.driverClasses)
                .map(candidate -> new DcErrorLogged(new DcClassloader(candidate, this.loader.classloader())))
                .collect(Collectors.toList());
        final Class<Driver> result;
        try {
            result = new DcIterator(candidates.iterator()).value();
        } catch (NoSuchElementException e) {
            throw new ConnectionFailure(
                String.format(
                    "Failed to create any of '%s' driver from driver path.", String.join(", ", this.driverClasses)
                )
            );
        }
        if (this.logger.isDebugEnabled()) {
            this.logger.debug(String.format("Using driver '%s'", result.getName()));
        }
        return result;
    }
}
