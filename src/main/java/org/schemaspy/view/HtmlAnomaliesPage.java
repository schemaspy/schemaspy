/*
 * Copyright (C) 2004 - 2011, 2014 John Currier
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2016 Ismail Simsek
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

import org.schemaspy.DbAnalyzer;
import org.schemaspy.model.Database;
import org.schemaspy.model.ForeignKeyConstraint;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This page lists all of the 'things that might not be quite right'
 * about the schema.
 *
 * @author John Currier
 * @author Rafal Kasa
 * @author Ismail Simsek
 */
public class HtmlAnomaliesPage extends HtmlFormatter {
    private static HtmlAnomaliesPage instance = new HtmlAnomaliesPage();

    /**
     * Singleton: Don't allow instantiation
     */
    private HtmlAnomaliesPage() {
    }

    /**
     * Singleton accessor
     *
     * @return the singleton instance
     */
    public static HtmlAnomaliesPage getInstance() {
        return instance;
    }

    public void write(Database database, Collection<Table> tables, List<? extends ForeignKeyConstraint> impliedConstraints, File outputDir) throws IOException {
        HashMap<String, Object> scopes = new HashMap<String, Object>();
        List<Table> unIndexedTables = DbAnalyzer.getTablesWithoutIndexes(new HashSet<Table>(tables));
        List<ForeignKeyConstraint> impliedConstraintColumns = impliedConstraints.stream().filter(c -> !c.getChildTable().isView()).collect(Collectors.toList());
        List<Table> oneColumnTables = DbAnalyzer.getTablesWithOneColumn(tables).stream().filter(t -> !t.isView()).collect(Collectors.toList());
        List<Table> incrementingColumnNames =  DbAnalyzer.getTablesWithIncrementingColumnNames(tables).stream().filter(t -> !t.isView()).collect(Collectors.toList());
        List<TableColumn> uniqueNullables = DbAnalyzer.getDefaultNullStringColumns(new HashSet<Table>(tables));

        scopes.put("displayNumRows", (displayNumRows ? new Object() : null));
        scopes.put("impliedConstraints", impliedConstraintColumns);
        scopes.put("unIndexedTables", unIndexedTables);
        scopes.put("oneColumnTables", oneColumnTables);
        scopes.put("incrementingColumnNames", incrementingColumnNames);
        scopes.put("uniqueNullables", uniqueNullables);

        MustacheWriter mw = new MustacheWriter( outputDir, scopes, getPathToRoot(), database.getName(), false);
        mw.write("anomalies.html", "anomalies.html", "anomalies.js");
    }
}
