package org.schemaspy.input.dbms.driverclass;

import java.sql.Driver;

/**
 * Encapsulates how to obtain a driverclass through a class loader.
 */
public class DcClassloader implements Driverclass {

    private final String candidate;
    private final ClassLoader loader;

    public DcClassloader(final String candidate, final ClassLoader loader) {
        this.candidate = candidate;
        this.loader = loader;
    }

    @Override
    public Class<Driver> value() throws ClassNotFoundException {
        return (Class<Driver>) Class.forName(this.candidate, true, this.loader);
    }
}
