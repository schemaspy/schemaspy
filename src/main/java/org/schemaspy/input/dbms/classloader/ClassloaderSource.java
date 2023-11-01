package org.schemaspy.input.dbms.classloader;

/**
 * Abstracts a strategy to access a classloader.
 */
public interface ClassloaderSource {

    /**
     * Asks the object to provide a Java classloader.
     * @return A standard library interface of the Java classloader.
     */
    ClassLoader classloader();
}
