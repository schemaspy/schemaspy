/*
 * This file is a part of the SchemaSpy project (http://schemaspy.org).
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
package org.schemaspy.view;

import org.schemaspy.model.Database;
import org.schemaspy.model.ForeignKeyConstraint;
import org.schemaspy.model.Table;
import org.schemaspy.util.LineWriter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * The page that lists all of the constraints in the schema
 *
 * @author John Currier
 */
public class HtmlConstraintsPage extends HtmlFormatter {
    private static HtmlConstraintsPage instance = new HtmlConstraintsPage();

    /**
     * Singleton: Don't allow instantiation
     */
    private HtmlConstraintsPage() {
    }

    /**
     * Singleton accessor
     *
     * @return the singleton instance
     */
    public static HtmlConstraintsPage getInstance() {
        return instance;
    }

    public void write(Database database, List<ForeignKeyConstraint> constraints, Collection<Table> tables, File outputDir) throws IOException {
        HashMap<String, Object> scopes = new HashMap<String, Object>();
        scopes.put("constraints", constraints);
        scopes.put("tables", tables);
        scopes.put("paginationEnabled",database.getConfig().isPaginationEnabled());

        MustacheWriter mw = new MustacheWriter( outputDir, scopes, getPathToRoot(), database.getName(), false);
        mw.write("constraint.html", "constraints.html", "constraint.js");
    }
}