package org.schemaspy.input.db.driver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PathResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Will split and check every path, if it's a directory it will look for files
     * ending with pattern ?ar (nar, jar, war) and return directory and files in a
     * URL set.
     *
     * @param driverPath contains list separated by {@link java.io.File}.pathSeparator
     * @return Set of URLs
     */
    public Set<URL> resolveDriverPath(String driverPath) {
        Set<URL> paths = Collections.emptySet();
        if (Objects.isNull(driverPath) || driverPath.isEmpty()) {
            return paths;
        }

        try (Stream<String> pathsStream = Arrays.stream(driverPath.split(File.pathSeparator))) {
            paths = pathsStream
                    .map(p -> Paths.get(p))
                    .filter(p -> {
                        if (!Files.exists(p)) {
                            LOGGER.warn(p.toString() + " doesn't exist");
                            return false;
                        }
                        return true;
                    })
                    .flatMap(addChildPaths)
                    .map(pathToUrl)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }
        if (paths.isEmpty()) {
            LOGGER.info("DriverPath resolved to: empty");
        } else {
            String strPaths = paths.stream()
                    .map(u -> u.getPath())
                    .collect(
                            Collectors.joining(
                                    System.lineSeparator() + "\t",
                                    System.lineSeparator() + "\t",
                                    "")
                    );
            LOGGER.info("DriverPath resolved to:" + strPaths);
        }
        return paths;
    }

    private static Function<Path, Stream<Path>> addChildPaths = p -> {
        if (Files.isDirectory(p)) {
            try {
                return Stream.concat(
                        Stream.of(p),
                        Files.list(p).filter(
                                childPath -> childPath.toString().matches(".*\\.?ar$"))
                );
            } catch (IOException e) {
                LOGGER.warn("Failed to read contents of folder " + p.toString(), e);
                return Stream.of(p);
            }
        }
        return Stream.of(p);
    };

    private static Function<Path, URL> pathToUrl = p -> {
        try {
            return p.toUri().toURL();
        } catch (MalformedURLException e) {
            LOGGER.warn("Unable to convert Path '" + p.toString() + "' to URL", e);
        }
        return null;
    };


}
