/*
 * This file is a part of the SchemaSpy project (http://schemaspy.org).
 * Copyright (C) 2011 John Currier
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

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.schemaspy.model.*;
import org.schemaspy.util.LineWriter;
import org.schemaspy.util.Markdown;

/**
 * The page that lists all of the routines (stored procedures and functions)
 * in the schema.
 *
 * @author John Currier
 */
public class HtmlRoutinesPage extends HtmlFormatter {
    private static HtmlRoutinesPage instance = new HtmlRoutinesPage();

    /**
     * Singleton: Don't allow instantiation
     */
    private HtmlRoutinesPage() {
    }

    /**
     * Singleton accessor
     *
     * @return the singleton instance
     */
    public static HtmlRoutinesPage getInstance() {
        return instance;
    }

    public void write(Database db, File outputDir) throws IOException {
        Collection<Routine> routines = new TreeSet<Routine>(db.getRoutines());

        HashMap<String, Object> scopes = new HashMap<String, Object>();
        scopes.put("routines", routines);
        scopes.put("paginationEnabled", db.getConfig().isPaginationEnabled());

        MustacheWriter mw = new MustacheWriter(outputDir, scopes, getPathToRoot(), db.getName(), false);
        mw.write("routines.html", "routines.html", "routines.js");

        for (Routine routine : routines) {
            writeRoutineFile(db, routine, outputDir);
        }
    }

    private void writeRoutineFile(Database db, Routine routine, File outputDir) {
        HtmlRoutinePage.getInstance().write(db,routine,outputDir);
    }

}