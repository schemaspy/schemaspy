/*
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
package org.schemaspy.view;

import org.schemaspy.model.Database;
import org.schemaspy.model.Routine;

import java.io.File;
import java.util.HashMap;

/**
 * @author Daniel Watt
 */
public class HtmlRoutinePage extends HtmlFormatter {
    private static HtmlRoutinePage instance = new HtmlRoutinePage();

    public static HtmlRoutinePage getInstance() {
        return instance;
    }

    public void write(Database db, Routine routine, File outputDir) {
        HashMap<String, Object> scopes = new HashMap<String, Object>();
        scopes.put("routine", routine);
        scopes.put("parameters",routine.getParameters());
        scopes.put("definitionExists",routine.getDefinition() != null);

        MustacheWriter mw = new MustacheWriter(outputDir, scopes, getPathToRoot(), db.getName(), false);
        mw.write("routines/routine.html", "routines/" + routine.getName() + ".html", "routine.js");
    }

    @Override protected String getPathToRoot() {
        return "../";
    }
}
