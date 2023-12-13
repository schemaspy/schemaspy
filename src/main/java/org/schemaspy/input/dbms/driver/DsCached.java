package org.schemaspy.input.dbms.driver;

import java.sql.Driver;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DsCached implements Driversource {

    private static Map<String, Driver> driverCache = new HashMap<>();

    private final Class<Driver> driverClass;
    private final Driversource origin;

    public DsCached(
        final Class<Driver> driverClass,
        final Driversource origin
    ) {
        this.driverClass = driverClass;
        this.origin = origin;
    }

    @Override
    public Driver driver() {
        Driver driver = driverCache.get(driverClass.getName());
        if (Objects.nonNull(driver)) {
            return driver;
        }
        driver = this.origin.driver();
        driverCache.put(driverClass.getName(), driver);
        return driver;
    }
}
