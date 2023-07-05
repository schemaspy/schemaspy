package org.schemaspy.input.dbms.classloader;

/**
 * Encapsulates how to access the default classloader
 */
public class ClDefault implements ClassloaderSource {

    public ClDefault() { }

    @Override
    public ClassLoader classloader() {
        return getClass().getClassLoader();
    }
}
