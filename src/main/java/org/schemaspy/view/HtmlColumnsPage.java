/*
 * Copyright (C) 2004 - 2011 John Currier
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2016 Ismail Simsek
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
package org.schemaspy.view;

import org.schemaspy.Config;
import org.schemaspy.model.Database;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The page that lists all of the columns in the schema,
 * allowing the end user to sort by column's attributes.
 *
 * @author John Currier
 * @author Rafal Kasa
 * @author Ismail Simsek
 * @author Thomas Traude
 * @author Nils Petzaell
 */
public class HtmlColumnsPage extends HtmlFormatter {
    private static HtmlColumnsPage instance = new HtmlColumnsPage();

    /**
     * Singleton: Don't allow instantiation
     */
    private HtmlColumnsPage() {
    }

    /**
     * Singleton accessor
     *
     * @return the singleton instance
     */
    public static HtmlColumnsPage getInstance() {
        return instance;
    }

    public void write(Database database, Collection<Table> tables, File outputDir) {
        Set<TableColumn> indexedColumns = tables.stream()
                .flatMap(table -> table.getIndexes().stream())
                .flatMap(tableIndex -> tableIndex.getColumns().stream())
                .collect(Collectors.toSet());

        Set<MustacheTableColumn> tableColumns = tables.stream()
                .flatMap(table -> table.getColumns().stream())
                .map(tableColumn -> new MustacheTableColumn(tableColumn, indexedColumns, getPathToRoot()))
                .collect(Collectors.toSet());

        HashMap<String, Object> scopes = new HashMap<String, Object>();
        scopes.put("columns", tableColumns);
        scopes.put("paginationEnabled",Config.getInstance().isPaginationEnabled());

        MustacheWriter mw = new MustacheWriter(outputDir, scopes, getPathToRoot(), database.getName(), false);
        mw.write("column.html", "columns.html", "column.js");
    }
}