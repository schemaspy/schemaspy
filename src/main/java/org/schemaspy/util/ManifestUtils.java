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
package org.schemaspy.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

/**
 * @author Nils Petzaell
 */
public class ManifestUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final Attributes manifestAttributes = new Attributes();

    static {
        try {
            URL codeSource = ManifestUtils.class.getProtectionDomain().getCodeSource().getLocation();
            URLConnection urlConnection = codeSource.openConnection();
            if (urlConnection instanceof JarURLConnection) {
                manifestAttributes.putAll(((JarURLConnection) urlConnection).getJarFile().getManifest().getMainAttributes());
            } else {
                manifestAttributes.putAll(new JarFile(codeSource.getFile()).getManifest().getMainAttributes());
            }
        } catch (IOException ioe) {
            LOGGER.debug("Failed to read manifest", ioe);
        }

    }

    private ManifestUtils() {}

    public static String getImplementationVersion() {
        return Optional.ofNullable(manifestAttributes.getValue("Implementation-Version")).orElse("IDE");
    }

    public static String getImplementationRevision() {
        return Optional.ofNullable(manifestAttributes.getValue("Implementation-Revision")).orElse("IDE");
    }
}
