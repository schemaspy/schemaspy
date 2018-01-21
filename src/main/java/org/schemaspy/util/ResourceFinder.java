package org.schemaspy.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class ResourceFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public InputStream find(String parent, String name) {
        parent = Objects.isNull(parent) ? "" : parent;
        InputStream inputStream = findFile(parent, name);
        if (Objects.isNull(inputStream)) {
            inputStream = findFile(System.getProperty("user.dir"), parent, name);
        }
        if (Objects.isNull(inputStream)) {
            inputStream = findFile(System.getProperty("user.home"), parent, name);
        }
        if (Objects.isNull(inputStream)) {
            if (parent.isEmpty()) {
                inputStream = findClassPath(name);
            } else {
                inputStream = findClassPath(parent.replaceFirst("^/", "") + "/" + name);
            }
        }
        if (Objects.isNull(inputStream)) {
            if (parent.isEmpty()) {
                throw new ResourceNotFoundException(name);
            } else {
                throw new ResourceNotFoundException(parent + File.separator + name);
            }
        }
        return inputStream;
    }

    private InputStream findFile(String first, String... more) {
        Path path = Paths.get(first, more);
        if (Files.exists(path)) {
            try {
                return Files.newInputStream(path);
            } catch (IOException e) {
                LOGGER.debug("Couldn't read existing file: " + path.toString(), e);
                return null;
            }
        }
        return null;
    }

    private InputStream findClassPath(String resource) {
        return this.getClass().getClassLoader().getResourceAsStream(resource);
    }
}