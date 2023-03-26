package org.schemaspy.util.copy;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Facade for simplifying copies from URLs.
 */
public final class CopyFromUrl implements Copy {

    private final URL resourceUrl;
    private final File targetPath;
    private final FileFilter filter;

    public CopyFromUrl(final URL resourceUrl, final File targetPath, final FileFilter filter) {
        this.resourceUrl = resourceUrl;
        this.targetPath = targetPath;
        this.filter = filter;
    }

    @Override
    public void copy() throws IOException {
        URLConnection url = resourceUrl.openConnection();
        if (url instanceof JarURLConnection) {
            new CopyFromJar((JarURLConnection) url, targetPath, filter).copy();
        }
    }
}
