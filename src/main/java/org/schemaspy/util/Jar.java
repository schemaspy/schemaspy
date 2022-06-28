package org.schemaspy.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.net.JarURLConnection;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Represents JAR file resources.
 */
class Jar {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final JarURLConnection jarConnection;
    private final File destPath;
    private final FileFilter filter;

    Jar(JarURLConnection jarConnection, File destPath, FileFilter filter) {
        this.jarConnection = jarConnection;
        this.destPath = destPath;
        this.filter = filter;
    }

    /**
     * Copies resources from the jar file of the current thread and extract it
     * to the destination path.
     */
    public void copyJarResourceToPath() {
        try {
            JarFile jarFile = jarConnection.getJarFile();
            String jarConnectionEntryName = jarConnection.getEntryName();

            /**
             * Iterate all entries in the jar file.
             */
            for (Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements();) {
                JarEntry jarEntry = e.nextElement(); //NOSONAR
                String jarEntryName = jarEntry.getName();

                /**
                 * Extract files only if they match the path.
                 */
                if (jarEntryName.startsWith(jarConnectionEntryName + "/")) {
                    String filename = jarEntryName.substring(jarConnectionEntryName.length());
                    File currentFile = new File(destPath, filename);


                    if (jarEntry.isDirectory()) {
                        FileUtils.forceMkdir(currentFile);
                    } else {
                        if (filter == null || filter.accept(currentFile)) {
                            try (
                                    InputStream is = jarFile.getInputStream(jarEntry);
                                    OutputStream out = Files.newOutputStream(
                                            currentFile.toPath(),
                                            StandardOpenOption.CREATE,
                                            StandardOpenOption.WRITE,
                                            StandardOpenOption.TRUNCATE_EXISTING)
                            ) {
                                IOUtils.copy(is, out);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.warn(e.getMessage(),e);
        }
    }
}
