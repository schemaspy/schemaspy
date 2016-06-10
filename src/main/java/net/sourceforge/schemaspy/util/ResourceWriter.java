/*
 * This file is a part of the SchemaSpy project (http://schemaspy.sourceforge.net).
 * Copyright (C) 2004, 2005, 2006, 2007, 2008, 2009, 2010 John Currier
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
package net.sourceforge.schemaspy.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ResourceWriter {
    private static ResourceWriter instance = new ResourceWriter();

    protected ResourceWriter() {
    }

    public static ResourceWriter getInstance() {
        return instance;
    }

    /**
     * Write the specified resource to the specified filename
     *
     * @param resourceName
     * @param writeTo
     * @throws IOException
     */
    public void writeResource(String resourceName, File writeTo) throws IOException {
        writeTo.getParentFile().mkdirs();
        InputStream in = getClass().getResourceAsStream(resourceName);
        if (in == null)
            throw new IOException("Resource \"" + resourceName + "\" not found");

        byte[] buf = new byte[4096];

        OutputStream out = new FileOutputStream(writeTo);
        int numBytes = 0;
        while ((numBytes = in.read(buf)) != -1) {
            out.write(buf, 0, numBytes);
        }
        in.close();
        out.close();
    }
}