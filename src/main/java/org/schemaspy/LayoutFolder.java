package org.schemaspy;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Objects;

/**
 * Represents the layout folder.
 */
public class LayoutFolder {

    private final ClassLoader classLoader;

    public LayoutFolder(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * The layout folder.
     *
     * @throws IOException when not possible to retrieve layout folder
     */
    public URL url() throws IOException {
        URL url = null;
        Enumeration<URL> possibleResources = classLoader.getResources("layout");
        while (possibleResources.hasMoreElements() && Objects.isNull(url)) {
            URL possibleResource = possibleResources.nextElement();
            if (!possibleResource.getPath().contains("test-classes")) {
                url = possibleResource;
            }
        }
        return url;
    }
}
