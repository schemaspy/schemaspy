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
package net.sourceforge.schemaspy.view;

import java.io.File;
import java.io.IOException;
import net.sourceforge.schemaspy.util.ResourceWriter;

public class ImageWriter extends ResourceWriter {
    private static ImageWriter instance = new ImageWriter();

    private ImageWriter() {
    }

    public static ImageWriter getInstance() {
        return instance;
    }

    public void writeImages(File outputDir) throws IOException {
        new File(outputDir, "images").mkdir();

        writeResource("/images/tabLeft.gif", new File(outputDir, "/images/tabLeft.gif"));
        writeResource("/images/tabRight.gif", new File(outputDir, "/images/tabRight.gif"));
        writeResource("/images/background.gif", new File(outputDir, "/images/background.gif"));
    }
}
