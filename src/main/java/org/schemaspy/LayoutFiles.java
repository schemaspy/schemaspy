package org.schemaspy;

import org.schemaspy.util.ResourceWriter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Objects;

/**
 * Represents the contents of the layout folder.
 */
public class LayoutFiles {

    private final ClassLoader classLoader;
    private final FileFilter filter;
    private final File outputDir;

    public LayoutFiles(ClassLoader classLoader, FileFilter filter, File outputDir) {
        this.classLoader = classLoader;
        this.filter = filter;
        this.outputDir = outputDir;
    }

    /**
     * Copy the files - sans template .html files - to object's destination.
     *
     * @throws IOException when not possible to copy layout files to outputDir
     */
    public void prepare() throws IOException {
        URL url = null;
        Enumeration<URL> possibleResources = classLoader.getResources("layout");
        while (possibleResources.hasMoreElements() && Objects.isNull(url)) {
            URL possibleResource = possibleResources.nextElement();
            if (!possibleResource.getPath().contains("test-classes")) {
                url = possibleResource;
            }
        }

        new ResourceWriter().copyResources(url, outputDir, filter);
    }
}
