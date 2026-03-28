package org.schemaspy.cli;

import org.schemaspy.input.dbms.exceptions.RuntimeIOException;
import org.schemaspy.util.JarFileRootPath;

import java.io.IOException;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        return connection instanceof JarURLConnection
            ? new JarFileRootPath(((JarURLConnection)connection).getJarFile()).toPath()
            : Paths.get(location.toURI());
    }
}
