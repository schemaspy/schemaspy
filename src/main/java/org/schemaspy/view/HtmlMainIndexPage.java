/*
 * Copyright (C) 2004 - 2011 John Currier
 * Copyright (C) 2016, 2017 Rafal Kasa
 * Copyright (C) 2016, 2017 Ismail Simsek
 * Copyright (C) 2017 Daniel Watt
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
import org.schemaspy.DbAnalyzer;
import org.schemaspy.model.Database;
import org.schemaspy.model.ForeignKeyConstraint;
import org.schemaspy.model.Table;
import org.schemaspy.util.Markdown;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * The main index that contains all tables and views that were evaluated
 *
 * @author John Currier
 * @author Rafal Kasa
 * @author Ismail Simsek
 * @author Daniel Watt
 * @author Nils Petzaell
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

    public void write(Database database, Collection<Table> tables, List<? extends ForeignKeyConstraint> impliedConstraints, File outputDir) throws IOException {
        Comparator<Table> sorter = new Comparator<Table>() {
            public int compare(Table table1, Table table2) {
                return table1.compareTo(table2);
            }
        };

        Collection<Table> remotes = database.getRemoteTables();
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
        long viewsAmount = tables.stream().filter(Table::isView).count();
        long constraintsAmount = DbAnalyzer.getForeignKeyConstraints(tables).size();
        long routinesAmount = database.getRoutines().size();
        long anomaliesAmount = getAllAnomaliesAmount(tables, impliedConstraints);

        HashMap<String, Object> scopes = new HashMap<String, Object>();
        scopes.put("tablesAmount", tablesAmount);
        scopes.put("viewsAmount", viewsAmount);
        scopes.put("columnsAmount", columnsAmount);
        scopes.put("constraintsAmount", constraintsAmount);
        scopes.put("routinesAmount", routinesAmount);
        scopes.put("anomaliesAmount", anomaliesAmount);

        scopes.put("tables", mustacheTables);
        scopes.put("database", database);
        scopes.put("databaseName", databaseName);
        scopes.put("description", Config.getInstance().getDescription());
        scopes.put("paginationEnabled", Config.getInstance().isPaginationEnabled());
        scopes.put("schema", new MustacheSchema(database.getSchema(), ""));
        scopes.put("catalog", new MustacheCatalog(database.getCatalog(), ""));
        
        MustacheWriter mw = new MustacheWriter(outputDir, scopes, "", database.getName(), false);
        mw.write("main.html", "index.html", "main.js");
    }

    private long getAllAnomaliesAmount(Collection<Table> tables, List<? extends ForeignKeyConstraint> impliedConstraints) {
        long anomalies = 0;
        anomalies += DbAnalyzer.getTablesWithoutIndexes(new HashSet<Table>(tables)).size();
        anomalies += impliedConstraints.stream().filter(c -> !c.getChildTable().isView()).count();
        anomalies += DbAnalyzer.getTablesWithOneColumn(tables).stream().filter(t -> !t.isView()).count();
        anomalies += DbAnalyzer.getTablesWithIncrementingColumnNames(tables).stream().filter(t -> !t.isView()).count();
        anomalies += DbAnalyzer.getDefaultNullStringColumns(new HashSet<Table>(tables)).size();

        return anomalies;
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
