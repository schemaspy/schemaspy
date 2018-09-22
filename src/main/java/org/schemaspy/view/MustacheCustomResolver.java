/*
 * Copyright (C) 2018 Nils Petzaell
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
package org.schemaspy.view;

import com.github.mustachejava.MustacheResolver;
import com.github.mustachejava.resolver.ClasspathResolver;
import com.github.mustachejava.resolver.FileSystemResolver;

import java.io.File;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.Optional;

public class MustacheCustomResolver implements MustacheResolver {

    private final MustacheResolver fileSystemResolver;
    private final MustacheResolver classpathResolver;

    public MustacheCustomResolver(String resourceRoot) {
        File fileRoot = Paths.get(resourceRoot).toFile();
        if (fileRoot.exists() && fileRoot.isDirectory()) {
            fileSystemResolver = new FileSystemResolver(Paths.get(resourceRoot).toFile());
        } else {
            fileSystemResolver = resourceName -> null;
        }
        classpathResolver = new ClasspathResolver(resourceRoot);
    }

    @Override
    public Reader getReader(String resourceName) {
        return Optional
                .ofNullable(fileSystemResolver.getReader(resourceName))
                .orElse(classpathResolver.getReader(resourceName));
    }

}
