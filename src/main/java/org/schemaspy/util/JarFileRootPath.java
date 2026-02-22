package org.schemaspy.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarFile;

public final class JarFileRootPath {

    private final JarFile jar;

    public JarFileRootPath(JarFile jar) {
        this.jar = jar;
    }

    public Path toPath() {
        String name = jar.getName();
        int separator = name.indexOf("!/");
        if (separator > 0) {
            name = name.substring(0, separator);
        }

        return Paths.get(name);
    }
}
