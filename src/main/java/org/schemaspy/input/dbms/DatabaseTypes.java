package org.schemaspy.input.dbms;

import org.schemaspy.input.dbms.exceptions.RuntimeIOException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DatabaseTypes {

    private Set<String> builtInTypes;

    public synchronized Set<String> getBuiltInDatabaseTypes() {
        if (Objects.isNull(builtInTypes)) {
            Enumeration<URL> typesFolders = urlsForTypesFolders();
            builtInTypes = new HashSet<>();
            while (typesFolders.hasMoreElements()) {
                URL typeFolder = typesFolders.nextElement();
                Path typeFolderPath = asPath(typeFolder);
                builtInTypes.addAll(collectDbTypes(typeFolderPath));
            }
        }
        return builtInTypes;
    }

    private Enumeration<URL> urlsForTypesFolders() {
        try {
            return getClass().getClassLoader().getResources("org/schemaspy/types");
        } catch (IOException e) {
            throw new RuntimeIOException("Unable to retrieve urls for type folders", e);
        }
    }

    private Path asPath(URL typeFolder) {
        try {
            if (typeFolder.getProtocol().equalsIgnoreCase("file")) {
                return Paths.get(typeFolder.toURI());
            }
            ensureFileSystemExists(typeFolder);
            URI uri = URI.create(typeFolder.toString().replace("classes!", "classes"));
            return Paths.get(uri);

        } catch (URISyntaxException | IOException e) {
            throw new RuntimeIOException("Unable to create Path for '" + typeFolder + "'", e);
        }
    }

    private void ensureFileSystemExists(URL url) throws URISyntaxException, IOException {
        try {
            FileSystems.getFileSystem(url.toURI());
        } catch (FileSystemNotFoundException notFound) {
            FileSystems.newFileSystem(url.toURI(), Collections.singletonMap("create", "false"));
        }
    }

    private Set<String> collectDbTypes(Path typeFolderPath) {
        try (Stream<Path> pathStream = Files.list(typeFolderPath)) {
            return pathStream
                .filter(Files::isRegularFile)
                .map(Path::getFileName)
                .map(Path::toString)
                .filter(name -> name.matches(".*\\.properties$"))
                .map(name -> name.replaceAll("\\.properties$", ""))
                .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeIOException("Unable to retrieve dbtypes from '" + typeFolderPath + "'", e);
        }
    }
}
