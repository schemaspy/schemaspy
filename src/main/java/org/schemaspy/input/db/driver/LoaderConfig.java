package org.schemaspy.input.db.driver;

public interface LoaderConfig {

    /**
     * Driver class to use
     *
     * @return class implementing {@link java.sql.Driver}
     */
    String getDriverClass();

    /**
     * Paths separated by pathSeparator in {@link java.io.File}
     * They will be used for custom classloading when retrieving
     * DriverClass.
     *
     * @return
     */
    String getDriverPath();
}
