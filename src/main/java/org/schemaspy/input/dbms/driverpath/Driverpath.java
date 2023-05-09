package org.schemaspy.input.dbms.driverpath;

/**
 * Abstracts a location to a JDBC driver.
 */
public interface Driverpath {

    /**
     * Asks the driver path to represent itself in text.
     * @return A textual representation of the JDBC driver location.
     */
    String value();
}
