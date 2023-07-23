package org.schemaspy.input.dbms.driverclass;

import java.sql.Driver;

/**
 * Abstracts the logic for obtaining a JDBC driver's class.
 */
public interface Driverclass {

    /**
     * Asks the object to provide the class.
     * @return A Java representation of a JDBC driver.
     * @throws ClassNotFoundException If the driver cannot be represented.
     */
    Class<Driver> value() throws ClassNotFoundException;
}
