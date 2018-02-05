/*
 * Copyright (C) 2017 Nils Petzaell
 */
package org.schemaspy.db.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * @author Nils Petzaell
 */
public class PropertiesFinder implements ResourceFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String DB_TYPES_LOCATION = "org/schemaspy/types/";
    private static final String DOT_PROPERTIES = ".properties";

    public URL find(String dbType) {
        URL url = findFile(dbType);
        if (Objects.isNull(url)) {
            url = findFile(dbType + DOT_PROPERTIES);
        }
        if (Objects.isNull(url)) {
            url = findClassPath(dbType);
        }
        if (Objects.isNull(url)) {
            url = findClassPath(dbType + DOT_PROPERTIES);
        }
        if (Objects.isNull(url)) {
            url = findClassPath(DB_TYPES_LOCATION + dbType);
        }
        if (Objects.isNull(url)) {
            url = findClassPath(DB_TYPES_LOCATION + dbType + DOT_PROPERTIES);
        }
        if (Objects.isNull(url)) {
            throw new ResourceNotFoundException(dbType);
        }
        return url;
    }

    private URL findFile(String file) {
        Path path = Paths.get(file);
        if (path.toFile().exists()) {
            try {
                return path.toUri().toURL();
            } catch (MalformedURLException e) {
                LOGGER.debug("Couldn't convert existing file: {}", path.toString(), e);
                return null;
            }
        }
        return null;
    }

    private URL findClassPath(String resource) {
        return this.getClass().getClassLoader().getResource(resource);
    }
}
