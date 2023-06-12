package org.schemaspy.input.dbms.driverclass;

import java.sql.Driver;
import java.util.Iterator;

/**
 * Encapsulates how to select a driverclass out of many.
 */
public class DcIterator implements Driverclass {

    private final Iterator<Driverclass> candidates;

    public DcIterator(final Iterator<Driverclass> candidates) {
        this.candidates = candidates;
    }

    @Override
    public Class<Driver> value() {
        try {
            return candidates.next().value();
        } catch (ClassNotFoundException e) {
            return value();
        }
    }
}
