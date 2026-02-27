package org.schemaspy.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarFile;

public final class JarFileRootPath {

    private String name;

    public JarFileRootPath(JarFile jar) {
        this(jar.getName());
    }

    public JarFileRootPath(String name) {
        this.name = name;
    }

    public Path toPath() {
        int separator = name.indexOf("!/");
        if (separator > 0) {
            name = name.substring(0, separator);
        }

        return Paths.get(name);
    }
}
