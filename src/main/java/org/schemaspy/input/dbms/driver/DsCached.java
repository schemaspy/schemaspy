package org.schemaspy.input.dbms.driver;

import java.sql.Driver;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DsCached implements Driversource {

    private static Map<String, Driver> driverCache = new HashMap<>();

    private final String[] driverClasses;
    private final String driverPath;
    private final Class<Driver> driverClass;
    private final Driversource origin;

    public DsCached(
        final String[] driverClasses,
        final String driverPath,
        final Class<Driver> driverClass,
        final Driversource origin
    ) {
        this.driverClasses = driverClasses;
        this.driverPath = driverPath;
        this.driverClass = driverClass;
        this.origin = origin;
    }

    @Override
    public Driver driver() {
        Driver driver;
        for (String driverClass: driverClasses) {
            driver = driverCache.get(driverClass + "|" + driverPath);
            if (Objects.nonNull(driver)) {
                return driver;
            }
        }
        driver = this.origin.driver();
        driverCache.put(driverClass.getName() + "|" + driverPath, driver);
        return driver;
    }
}
