package org.schemaspy.input.dbms;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class GetExistingUrls {

    /**
     * Returns a list of {@link URL}s in <code>path</code> that point to files that
     * exist.
     *
     * @param path
     * @return
     */
    public Set<URI> getExistingUrls(String path) {
        Set<URI> existingUrls = new HashSet<>();

        String[] pieces = path.split(File.pathSeparator);
        for (String piece : pieces) {
            File file = new File(piece);
            if (file.exists()) {
                existingUrls.add(file.toURI());
                if (file.isDirectory()) {
                    addDirectoryContent(file, existingUrls);
                }
            }
        }

        return existingUrls;
    }

    private void addDirectoryContent(File dir, Set<URI> existingUrls) {
        File[] files = dir.listFiles();
        for(File file : files) {
            if (file.exists()) {
                existingUrls.add(file.toURI());
                if (file.isDirectory()) {
                    addDirectoryContent(file, existingUrls);
                }
            }
        }
    }
}
