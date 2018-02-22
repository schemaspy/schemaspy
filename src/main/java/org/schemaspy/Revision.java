/*
 * Copyright (C) 2004-2011 John Currier
 * Copyright (C) 2018 Nils Petzaell
 *
 * This file is a part of the SchemaSpy project (http://schemaspy.org).
 *
 * SchemaSpy is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.schemaspy;

import org.schemaspy.util.JarFileFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

/**
 * @author John Currier
 * @author Nils Petzaell
 */
public class Revision {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final JarFileFinder jarFileFinder = new JarFileFinder();

	private static String rev          = "Unknown";

	static {
		initialize();
	}

	private static void initialize() {
		try (JarFile jarFile = jarFileFinder.findJarFileForClass(Revision.class)) {
			Attributes main     = jarFile.getManifest().getMainAttributes();
			rev = (String) main.getOrDefault(new Attributes.Name("Implementation-Build"),"Unknown");
		} catch (IOException ex) {
			LOGGER.warn(ex.getMessage(), ex);
		}
	}

	@Override
	public String toString() {
		return rev;
	}
}