package org.schemaspy.input.dbms.classloader;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.StreamSupport;

import org.schemaspy.input.dbms.driverpath.Driverpath;
import org.schemaspy.util.Filtered;
import org.schemaspy.util.Mapped;

/**
 * Encapsulates how to access the classloader specified by a classpath.
 */
public final class ClClasspath implements ClassloaderSource {

    private final Iterable<URL> classpath;

    public ClClasspath(final Driverpath driverpath) {
        this.classpath =
          new Filtered<>(
            new Mapped<>(
              new Mapped<>(
                driverpath,
                Path::toUri
              ),
              uri -> {
                  try {
                      return uri.toURL();
                  } catch (MalformedURLException e) {
                      return null;
                  }
              }
              ),
            Objects::nonNull
          );
    }

    @Override
    public ClassLoader classloader() {
        // if a classpath has been specified then use it to find the driver,
        // otherwise use whatever was used to load this class.
        // thanks to Bruno Leonardo Gonalves for this implementation that he
        // used to resolve issues when running under Maven

        final URL[] urls = StreamSupport.stream(classpath.spliterator(), false)
                             .toArray(URL[]::new);

        return new URLClassLoader(
                urls,
                new ClDefault().classloader()
        );
    }
}
