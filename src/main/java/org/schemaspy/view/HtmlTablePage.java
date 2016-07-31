/*
 * This file is a part of the SchemaSpy project (http://schemaspy.org).
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
package org.schemaspy.view;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.schemaspy.Config;
import org.schemaspy.model.Database;
import org.schemaspy.model.ForeignKeyConstraint;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.schemaspy.model.TableIndex;
import org.schemaspy.util.DiagramUtil;
import org.schemaspy.util.Dot;
import org.schemaspy.util.LineWriter;
import org.schemaspy.util.Markdown;

/**
 * The page that contains the details of a specific table or view
 *
 * @author John Currier
 */
public class HtmlTablePage extends HtmlFormatter {
    private static final HtmlTablePage instance = new HtmlTablePage();
    private int columnCounter = 0;

    private final Map<String, String> defaultValueAliases = new HashMap<String, String>();
    {
        defaultValueAliases.put("CURRENT TIMESTAMP", "now"); // DB2
        defaultValueAliases.put("CURRENT TIME", "now");      // DB2
        defaultValueAliases.put("CURRENT DATE", "now");      // DB2
        defaultValueAliases.put("SYSDATE", "now");           // Oracle
        defaultValueAliases.put("CURRENT_DATE", "now");      // Oracle
    }

    /**
     * Singleton: Don't allow instantiation
     */
    private HtmlTablePage() {
    }

    /**
     * Singleton accessor
     *
     * @return the singleton instance
     */
    public static HtmlTablePage getInstance() {
        return instance;
    }

    public WriteStats write(Database db, Table table, File outputDir, WriteStats stats) throws IOException {

        writeMainTable(db, table, outputDir, stats);

        return stats;
    }

    public void writeMainTable(Database db, Table table, File outputDir, WriteStats stats) throws IOException {
        Set<TableColumn> primaries = new HashSet<TableColumn>(table.getPrimaryColumns());
        Set<TableColumn> indexes = new HashSet<TableColumn>();
        Set<MustacheTableColumn> tableColumns = new LinkedHashSet<>();
        Set<MustacheTableIndex> indexedColumns = new LinkedHashSet<>();
        Set<TableIndex> sortIndexes = new TreeSet<TableIndex>(table.getIndexes()); // sort primary keys first

        for (TableIndex index : sortIndexes) {
            indexes.addAll(index.getColumns());
            indexedColumns.add(new MustacheTableIndex(index));
        }

        for (TableColumn column : table.getColumns()) {
            tableColumns.add(new MustacheTableColumn(column, indexes,getPathToRoot()));
        }

        HashMap<String, Object> scopes = new HashMap<String, Object>();
        scopes.put("table", table);
        scopes.put("comments", Markdown.toHtml(table.getComments(),getPathToRoot()));
        scopes.put("primaries", primaries);
        scopes.put("columns", tableColumns);
        scopes.put("indexes", indexedColumns);

        List<MustacheTableDiagram> diagrams = new ArrayList<>();
        Object graphvizExists = generateDiagrams(table, stats, outputDir, diagrams);
        String graphvizVersion = Dot.getInstance().getSupportedVersions().substring(4);
        scopes.put("graphvizExists", graphvizExists);
        scopes.put("graphvizVersion", graphvizVersion);

        scopes.put("diagrams", diagrams);
        scopes.put("sqlCode", sqlCode(table));
        scopes.put("references", sqlReferences(table, db));

        scopes.put("diagramExists", DiagramUtil.diagramExists(diagrams));
        scopes.put("indexExists", indexExists(table, indexedColumns));
        scopes.put("definitionExists", definitionExists(table));

        MustacheWriter mw = new MustacheWriter(outputDir, scopes, getPathToRoot(), db.getName());
        mw.write("/layout/tables/table.html", Markdown.pagePath(table.getName()), "table.js");
    }



    private Set<Table> sqlReferences(Table table, Database db) {
        Set<Table> references = null;

        if  (table.isView() && table.getViewSql() != null) {
            DefaultSqlFormatter formatter = new DefaultSqlFormatter();
            references = formatter.getReferencedTables(table.getViewSql(), db);
        }
        return references;
    }

    private String sqlCode(Table table) {
        return table.getViewSql() != null ? table.getViewSql().trim() : "";
    }

    private Object indexExists(Table table,  Set<MustacheTableIndex> indexedColumns) {
        Object exists = null;
        if  (!table.isView() && indexedColumns.size() > 0) {
            exists = new Object();
        }
        return exists;
    }

    private Object definitionExists(Table table) {
        Object exists = null;
        if  (table.isView() && table.getViewSql() != null) {
            exists = new Object();
        }
        return exists;
    }

    /**
     * Generate the .dot file(s) to represent the specified table's relationships.
     *
     * Generates a <TABLENAME>.dot if the table has real relatives.
     *
     * Also generates a <TABLENAME>.implied2degrees.dot if the table has implied relatives within
     * two degrees of separation.
     *
     * @param table Table
     * @param diagramsDir File
     * @throws IOException
     * @return boolean <code>true</code> if the table has implied relatives within two
     *                 degrees of separation.
     */
    private boolean generateDots(Table table, File diagramDir, WriteStats stats) throws IOException {
        Dot dot = Dot.getInstance();
        String extension = dot == null ? "png" : dot.getFormat();

        File oneDegreeDotFile = new File(diagramDir, table.getName() + ".1degree.dot");
        File oneDegreeDiagramFile = new File(diagramDir, table.getName() + ".1degree." + extension);
        File twoDegreesDotFile = new File(diagramDir, table.getName() + ".2degrees.dot");
        File twoDegreesDiagramFile = new File(diagramDir, table.getName() + ".2degrees." + extension);
        File oneImpliedDotFile = new File(diagramDir, table.getName() + ".implied1degrees.dot");
        File oneImpliedDiagramFile = new File(diagramDir, table.getName() + ".implied1degrees." + extension);
        File twoImpliedDotFile = new File(diagramDir, table.getName() + ".implied2degrees.dot");
        File twoImpliedDiagramFile = new File(diagramDir, table.getName() + ".implied2degrees." + extension);

        // delete before we start because we'll use the existence of these files to determine
        // if they should be turned into pngs & presented
        oneDegreeDotFile.delete();
        oneDegreeDiagramFile.delete();
        twoDegreesDotFile.delete();
        twoDegreesDiagramFile.delete();
        oneImpliedDotFile.delete();
        oneImpliedDiagramFile.delete();
        twoImpliedDotFile.delete();
        twoImpliedDiagramFile.delete();


        if (table.getMaxChildren() + table.getMaxParents() > 0) {
            Set<ForeignKeyConstraint> impliedConstraints;

            DotFormatter formatter = DotFormatter.getInstance();
            LineWriter dotOut = new LineWriter(oneDegreeDotFile, Config.DOT_CHARSET);
            WriteStats oneStats = new WriteStats(stats);
            formatter.writeRealRelationships(table, false, oneStats, dotOut);
            dotOut.close();

            dotOut = new LineWriter(twoDegreesDotFile, Config.DOT_CHARSET);
            WriteStats twoStats = new WriteStats(stats);
            impliedConstraints = formatter.writeRealRelationships(table, true, twoStats, dotOut);
            dotOut.close();

            if (oneStats.getNumTablesWritten() + oneStats.getNumViewsWritten() == twoStats.getNumTablesWritten() + twoStats.getNumViewsWritten()) {
                twoDegreesDotFile.delete(); // no different than before, so don't show it
            }

            if (!impliedConstraints.isEmpty()) {
                dotOut = new LineWriter(oneImpliedDotFile, Config.DOT_CHARSET);
                formatter.writeAllRelationships(table, false, stats, dotOut);
                dotOut.close();

                dotOut = new LineWriter(twoImpliedDotFile, Config.DOT_CHARSET);
                formatter.writeAllRelationships(table, true, stats, dotOut);
                dotOut.close();
                return true;
            }
        }

        return false;
    }

    private Object generateDiagrams(Table table, WriteStats stats, File outputDir, List<MustacheTableDiagram> diagrams) throws IOException {
        Object graphviz = new Object();;
        File diagramsDir = new File(outputDir, "diagrams");
        boolean hasImplied = generateDots(table, diagramsDir, stats);

        if (table.getMaxChildren() + table.getMaxParents() > 0)
        {
            if (HtmlTableDiagrammer.getInstance().write(table, diagramsDir, diagrams)) {
                //writeExcludedColumns(stats.getExcludedColumns(), table, html);
            } else {
                graphviz = null;
            }
        }

        return graphviz;
    }

    @Override
    protected String getPathToRoot() {
        return "../";
    }
}