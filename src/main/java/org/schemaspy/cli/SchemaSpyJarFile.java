package org.schemaspy.cli;

import org.schemaspy.input.dbms.exceptions.RuntimeIOException;

import java.io.IOException;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarFile;

public class SchemaSpyJarFile {
    public Path path() {
        URL location = getClass().getProtectionDomain().getCodeSource().getLocation();
        try {
            return findSource(location);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeIOException("Unable to get SchemaSpy-jarFile path", e);
        }
    }

    private Path findSource(URL location) throws IOException, URISyntaxException {
        URLConnection connection = location.openConnection();
        return connection instanceof JarURLConnection ? getRootJarFile(((JarURLConnection)connection).getJarFile()) : Paths.get(location.toURI());
    }

    private Path getRootJarFile(JarFile jarFile) {
        String name = jarFile.getName();
        int separator = name.indexOf("!/");
        if (separator > 0) {
            name = name.substring(0, separator);
        }

        return Paths.get(name);
    }
}
