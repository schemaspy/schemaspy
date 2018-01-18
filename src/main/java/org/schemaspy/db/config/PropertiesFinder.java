/*
 * Copyright (C) 2017 Nils Petzaell
 */
package org.schemaspy.db.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
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

    public URL find(final String dbType) {
        String dbTypeToFind = addExtensionIfMissing(dbType);
        URL url = findFile(dbTypeToFind);
        if (Objects.isNull(url)) {
            url = findClassPath(dbTypeToFind);
        }
        if (Objects.isNull(url)) {
            url = findClassPath(DB_TYPES_LOCATION + dbTypeToFind);
        }
        if (Objects.isNull(url)) {
            throw new ResourceNotFoundException(dbType);
        }
        return url;
    }

    private String addExtensionIfMissing(String dbType) {
        return dbType.toLowerCase().endsWith(".properties") ? dbType : dbType + ".properties";
    }

    private URL findFile(String file) {
        Path path = Paths.get(file);
        if (path.toFile().exists() && path.toFile().isFile()) {
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
        URL url = this.getClass().getClassLoader().getResource(resource);
        try {
            if (Objects.nonNull(url) && Paths.get(url.toURI()).toFile().isFile()) {
                return url;
            }
        } catch (URISyntaxException e) {
            LOGGER.debug("Couldn't convert url to uri to file: {}", url, e);
            return null;
        }
        return null;
    }
}
