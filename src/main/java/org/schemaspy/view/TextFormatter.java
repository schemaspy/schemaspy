/*
 * Copyright (C) 2004 - 2011 John Currier
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
package org.schemaspy.view;

import org.schemaspy.model.Table;
import org.schemaspy.util.LineWriter;

import java.io.IOException;
import java.util.Collection;

/**
 * @author John Currier
 */
public class TextFormatter {
    private static TextFormatter instance = new TextFormatter();

    /**
     * Singleton - prevent creation
     */
    private TextFormatter() {
    }

    public static TextFormatter getInstance() {
        return instance;
    }

    public void write(Collection<Table> tables, boolean includeViews, LineWriter out) throws IOException {
        for (Table table : tables) {
            if (!table.isView() || includeViews)
                out.writeln(table.getName());
        }
    }
}
