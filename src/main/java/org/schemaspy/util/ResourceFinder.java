/*
 * Copyright (C) 2017 Nils Petzaell
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

/**
 * @author Nils Petzaell
 */
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