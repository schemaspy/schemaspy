package org.schemaspy.input.dbms.classloader;

import org.schemaspy.input.dbms.classpath.Classpath;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Encapsulates how to access the classloader specified by a classpath.
 */
public final class ClClasspath implements ClassloaderSource {

    private final Classpath classpath;

    public ClClasspath(final Classpath classpath) {
        this.classpath = classpath;
    }

    @Override
    public ClassLoader classloader() {
        // if a classpath has been specified then use it to find the driver,
        // otherwise use whatever was used to load this class.
        // thanks to Bruno Leonardo Gonalves for this implementation that he
        // used to resolve issues when running under Maven

        final List<URL> urls = this.classpath.paths().stream().map(uri -> {
            try {
                return uri.toURL();
            } catch (MalformedURLException e) {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());

        return new URLClassLoader(
                urls.toArray(new URL[urls.size()]),
                new ClDefault().classloader()
        );
    }
}
