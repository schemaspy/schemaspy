package org.schemaspy.input.dbms.classpath;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.schemaspy.input.dbms.driverpath.Driverpath;

public class GetExistingUrls implements Classpath {

    private final Driverpath driverpath;

    public GetExistingUrls(final Driverpath driverpath) {

        this.driverpath = driverpath;
    }

    @Override
    public Set<URI> paths() {
        final String[] pieces = this.driverpath.value().split(File.pathSeparator);
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
