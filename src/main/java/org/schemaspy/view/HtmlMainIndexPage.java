/*
 * This file is a part of the SchemaSpy project (http://schemaspy.org).
 * Copyright (C) 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011 John Currier
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

import org.schemaspy.DbAnalyzer;
import org.schemaspy.model.Database;
import org.schemaspy.model.Table;
import org.schemaspy.util.Markdown;

/**
 * The main index that contains all tables and views that were evaluated
 *
 * @author John Currier
 */
public class HtmlMainIndexPage extends HtmlFormatter {
    private static HtmlMainIndexPage instance = new HtmlMainIndexPage();

    /**
     * Singleton: Don't allow instantiation
     */
    private HtmlMainIndexPage() {
    }

    /**
     * Singleton accessor
     *
     * @return the singleton instance
     */
    public static HtmlMainIndexPage getInstance() {
        return instance;
    }

    public void write(Database database, Collection<Table> tables, Collection<Table> remotes, File outputDir) throws IOException {
        Comparator<Table> sorter = new Comparator<Table>() {
            public int compare(Table table1, Table table2) {
                return table1.compareTo(table2);
            }
        };
        // sort tables and remotes by name
        Collection<Table> tmp = new TreeSet<Table>(sorter);
        tmp.addAll(tables);
        tables = tmp;
        tmp = new TreeSet<Table>(sorter);
        tmp.addAll(remotes);

        String databaseName = getDatabaseName(database);

        List<MustacheTable> mustacheTables = new ArrayList<>();

        long columnsAmount = 0;

        for(Table table: tables) {
            columnsAmount += table.getColumns().size();
            String comments = Markdown.toHtml(table.getComments(), "");
            MustacheTable mustacheTable = new MustacheTable(table, "");
            mustacheTable.setComments(comments);
            mustacheTables.add(mustacheTable);
        }

        long tablesAmount = tables.stream().filter(t -> !t.isView()).count();
        long viewsAmount = tables.stream().filter(v -> v.isView()).count();
        long constraintsAmount = DbAnalyzer.getForeignKeyConstraints(tables).size();

        HashMap<String, Object> scopes = new HashMap<String, Object>();
        scopes.put("tablesAmount", tablesAmount);
        scopes.put("viewsAmount", viewsAmount);
        scopes.put("columnsAmount", columnsAmount);
        scopes.put("constraintsAmount", constraintsAmount);

        scopes.put("tables", mustacheTables);
        scopes.put("database", database);
        scopes.put("databaseName", databaseName);
        scopes.put("description", database.getDescription());
        scopes.put("paginationEnabled",database.getConfig().isPaginationEnabled());
        scopes.put("schema", new MustacheSchema(database.getSchema(), ""));
        scopes.put("catalog", new MustacheCatalog(database.getCatalog(), ""));
        
        MustacheWriter mw = new MustacheWriter(outputDir, scopes, "", database.getName(), false);
        mw.write("main.html", "index.html", "main.js");
    }

    private String getDatabaseName(Database db) {
        StringBuilder description = new StringBuilder();

        description.append(db.getName());
        if (db.getSchema() != null) {
            description.append('.');
            description.append(db.getSchema().getName());
        } else if (db.getCatalog() != null) {
            description.append('.');
            description.append(db.getCatalog().getName());
        }

        return description.toString();
    }
}
