package org.schemaspy.input.dbms.driver;

import java.sql.Driver;
import java.util.ArrayList;
import java.util.List;

public class DsCached implements Driversource {

    private final Driversource origin;
    private final List<Driver> cache;

    public DsCached(final Driversource origin) {
        this(origin, new ArrayList<>());
    }

    public DsCached(final Driversource origin, final List<Driver> cache) {
        this.origin = origin;
        this.cache = cache;
    }

    @Override
    public Driver driver() {
        if (this.cache.isEmpty()) {
            this.cache.add(this.origin.driver());
        }
        return this.cache.get(0);
    }
}
