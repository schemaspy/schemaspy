/*
 * This file is a part of the SchemaSpy project (http://schemaspy.sourceforge.net).
 * Copyright (C) 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2014 John Currier
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
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import net.sourceforge.schemaspy.DbAnalyzer;
import net.sourceforge.schemaspy.model.Database;
import net.sourceforge.schemaspy.model.ForeignKeyConstraint;
import net.sourceforge.schemaspy.model.Table;
import net.sourceforge.schemaspy.model.TableColumn;
import net.sourceforge.schemaspy.util.LineWriter;

/**
 * This page lists all of the 'things that might not be quite right'
 * about the schema.
 *
 * @author John Currier
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

        MustacheWriter mw = new MustacheWriter( outputDir, scopes, getPathToRoot(), database.getName());
        mw.write("/layout/anomalies.html", "anomalies.html", "anomalies.js");
    }

    @Override
    protected boolean isAnomaliesPage() {
        return true;
    }
}
