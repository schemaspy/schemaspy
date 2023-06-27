package org.schemaspy.input.dbms.classpath;

import org.schemaspy.input.dbms.classpath.Classpath;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class GetExistingUrls implements Classpath {

    private final String path;

    public GetExistingUrls(final String path) {
        this.path = path;
    }

    @Override
    public Set<URI> paths() {
        String[] pieces = path.split(File.pathSeparator);
        Iterable<File> files = Arrays.stream(pieces).map(File::new).collect(Collectors.toList());
        return consider(files);
    }

    private Set<URI> addDirectoryContent(File dir) {
        return consider(() -> Arrays.stream(dir.listFiles()).iterator());
    }

    private Set<URI> consider(Iterable<File> files) {
        Set<URI> result = new HashSet<>();
        for(File file : files) {
            if (file.exists()) {
                result.add(file.toURI());
                if (file.isDirectory()) {
                    result.addAll(addDirectoryContent(file));
                }
            }
        }
        return result;
    }
}
