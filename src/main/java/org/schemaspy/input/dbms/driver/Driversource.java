package org.schemaspy.input.dbms.driver;

import java.sql.Driver;

/**
 * Abstracts a strategy to access a JDBC driver.
 */
public interface Driversource {

    /**
     * Asks the object to provide a JDBC driver.
     * @return A standard library interface of the JDBC driver.
     */
    Driver driver();
}
