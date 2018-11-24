/*
 * Copyright (C) 2017, 2018 Nils Petzaell
 * Copyright (C) 2017 Daniel Watt
 *
 * This file is part of SchemaSpy.
 *
 * SchemaSpy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SchemaSpy. If not, see <http://www.gnu.org/licenses/>.
 */
package org.schemaspy.db.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Collections;
import java.util.Objects;

/**
 * @author Nils Petzaell
 * @author Daniel Watt
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
        if (Objects.nonNull(url)) {
            try {
                Path path;
                if ("jar".equals(url.getProtocol())) {
                    ensureFileSystemExists(url);
                    path = fixSpringBootPath(url.toString());
                } else {
                    path = Paths.get(url.toURI());
                }
                if (Files.isRegularFile(path)) { //NOSONAR toFile().isFile() doesn't work with Zip filesystem
                    return url;
                }
            } catch (URISyntaxException e) {
                LOGGER.debug("Couldn't convert url to uri to file: {}", url, e);
                return null;
            } catch (IOException e) {
                LOGGER.error("Unable to create filesystem for url: {}", url.toString(), e);
            }
        }
        return null;
    }

    private Path fixSpringBootPath(String s) {
        URI uri = URI.create(s.replace("classes!","classes"));
        return Paths.get(uri);
    }

    private void ensureFileSystemExists(URL url) throws URISyntaxException, IOException {
        try {
            FileSystems.getFileSystem(url.toURI());
        } catch (FileSystemNotFoundException notFound) {
            FileSystems.newFileSystem(url.toURI(), Collections.singletonMap("create","false"));
        }
    }
}
