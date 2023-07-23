package org.schemaspy.input.dbms.classpath;

import org.schemaspy.input.dbms.ConnectionConfig;

import java.net.URI;
import java.util.Set;

/**
 * Includes sibling jar files in the classpath, based on configurations.
 */
public class WithSiblings implements Classpath {

    private final ConnectionConfig connectionConfig;
    private final Classpath base;
    private final Classpath siblings;

    public WithSiblings(final ConnectionConfig configs, final Classpath base, final Classpath siblings) {
        this.connectionConfig = configs;
        this.base = base;
        this.siblings = siblings;
    }

    @Override
    public Set<URI> paths() {
        final Set<URI> result = this.base.paths();
        if (this.connectionConfig.withLoadSiblings()) {
            result.addAll(siblings.paths());
        }
        return result;
    }
}
