package org.schemaspy.input.dbms.classpath;

import java.net.URI;
import java.util.Set;

/**
 * Abstracts the locations of external class files.
 */
public interface Classpath {

    /**
     * Asks the driver path to represent itself as a set of URIs.
     * @return The set of all external class files' locations.
     */
    Set<URI> paths();
}
