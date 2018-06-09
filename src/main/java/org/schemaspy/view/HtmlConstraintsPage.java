/*
 * Copyright (C) 2004 - 2010 John Currier
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2016 Ismail Simsek
 * Copyright (C) 2017 Daniel Watt
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

import org.schemaspy.Config;
import org.schemaspy.model.Database;
import org.schemaspy.model.ForeignKeyConstraint;
import org.schemaspy.model.Table;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The page that lists all of the constraints in the schema
 *
 * @author John Currier
 * @author Rafal Kasa
 * @author Ismail Simsek
 * @author Thomas Traude
 * @author Daniel Watt
 * @author Nils Petzaell
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
        scopes.put("checkConstraints", collectCheckConstraints(tables));
        scopes.put("paginationEnabled", Config.getInstance().isPaginationEnabled());

        MustacheWriter mw = new MustacheWriter( outputDir, scopes, getPathToRoot(), database.getName(), false);
        mw.write("constraint.html", "constraints.html", "constraint.js");
    }

    private static List<MustacheCheckConstraint> collectCheckConstraints(Collection<Table> tables) {
        return tables.stream()
                .filter(table -> table.getCheckConstraints().size() > 0)
                .flatMap(table -> table.getCheckConstraints().entrySet()
                        .stream()
                        .map(entry -> new MustacheCheckConstraint(
                                table.getName(),
                                entry.getKey(),
                                entry.getValue())
                        )
                ).collect(Collectors.toList());
    }
}