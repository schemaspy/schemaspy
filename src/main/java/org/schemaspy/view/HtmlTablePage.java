/*
 * Copyright (C) 2004 - 2011, 2014 John Currier
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2016 Ismail Simsek
 * Copyright (C) 2017 Wojciech Kasa
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
import org.schemaspy.model.*;
import org.schemaspy.util.DiagramUtil;
import org.schemaspy.util.Dot;
import org.schemaspy.util.LineWriter;
import org.schemaspy.util.Markdown;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

/**
 * The page that contains the details of a specific table or view
 *
 * @author John Currier
 * @author Rafal Kasa
 * @author Ismail Simsek
 * @author Wojciech Kasa
 * @author Daniel Watt
 * @author Nils Petzaell
 */
public class HtmlTablePage extends HtmlFormatter {

    private SqlAnalyzer sqlAnalyzer;

    public HtmlTablePage(Database database) {
        sqlAnalyzer = new SqlAnalyzer(database.getDbmsMeta().getAllKeywords(), database.getTables(), database.getViews());
    }

    public void write(Database db, Table table, File outputDir, WriteStats stats) throws IOException {
        Set<TableColumn> primaries = new HashSet<>(table.getPrimaryColumns());
        Set<TableColumn> indexes = new HashSet<>();
        Set<MustacheTableColumn> tableColumns = new LinkedHashSet<>();
        Set<MustacheTableIndex> indexedColumns = new LinkedHashSet<>();
        Set<TableIndex> sortIndexes = new TreeSet<>(table.getIndexes()); // sort primary keys first

        for (TableIndex index : sortIndexes) {
            indexes.addAll(index.getColumns());
            indexedColumns.add(new MustacheTableIndex(index));
        }

        for (TableColumn column : table.getColumns()) {
            tableColumns.add(new MustacheTableColumn(column, indexes, getPathToRoot()));
        }

        HashMap<String, Object> scopes = new HashMap<>();
        scopes.put("table", table);
        scopes.put("comments", Markdown.toHtml(table.getComments(), getPathToRoot()));
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
        scopes.put("references", sqlReferences(table));

        scopes.put("diagramExists", DiagramUtil.diagramExists(diagrams));
        scopes.put("indexExists", indexExists(table, indexedColumns));
        scopes.put("definitionExists", definitionExists(table));
        MustacheWriter mw = new MustacheWriter(outputDir, scopes, getPathToRoot(), db.getName(), false);
        mw.write("tables/table.html", Markdown.pagePath(table.getName()), "table.js");
    }


    private Set<Table> sqlReferences(Table table) {
        Set<Table> references = null;

        if (table.isView() && table.getViewDefinition() != null) {
            references = sqlAnalyzer.getReferencedTables(table.getViewDefinition());
        }
        return references;
    }

    private static String sqlCode(Table table) {
        return table.getViewDefinition() != null ? table.getViewDefinition().trim() : "";
    }

    private Object indexExists(Table table, Set<MustacheTableIndex> indexedColumns) {
        Object exists = null;
        if (!table.isView() && !indexedColumns.isEmpty()) {
            exists = new Object();
        }
        return exists;
    }

    private static Object definitionExists(Table table) {
        Object exists = null;
        if (table.isView() && table.getViewDefinition() != null) {
            exists = new Object();
        }
        return exists;
    }

    /**
     * Generate the .dot file(s) to represent the specified table's relationships.
     * <p>
     * Generates a <TABLENAME>.dot if the table has real relatives.
     * <p>
     * Also generates a <TABLENAME>.implied2degrees.dot if the table has implied relatives within
     * two degrees of separation.
     *
     * @param table       Table
     * @param diagramDir File
     * @return boolean <code>true</code> if the table has implied relatives within two
     * degrees of separation.
     * @throws IOException
     */
    private boolean generateDots(Table table, File diagramDir, WriteStats stats, File outputDir) throws IOException {
        Dot dot = Dot.getInstance();
        String extension = dot == null ? Config.getInstance().getImageFormat() : dot.getFormat();

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
        Files.deleteIfExists(oneDegreeDotFile.toPath());
        Files.deleteIfExists(oneDegreeDiagramFile.toPath());
        Files.deleteIfExists(twoDegreesDotFile.toPath());
        Files.deleteIfExists(twoDegreesDiagramFile.toPath());
        Files.deleteIfExists(oneImpliedDotFile.toPath());
        Files.deleteIfExists(oneImpliedDiagramFile.toPath());
        Files.deleteIfExists(twoImpliedDotFile.toPath());
        Files.deleteIfExists(twoImpliedDiagramFile.toPath());


        if (table.getMaxChildren() + table.getMaxParents() > 0) {
            Set<ForeignKeyConstraint> impliedConstraints;

            DotFormatter formatter = DotFormatter.getInstance();
            LineWriter dotOut = new LineWriter(oneDegreeDotFile, Config.DOT_CHARSET);
            WriteStats oneStats = new WriteStats(stats);
            formatter.writeRealRelationships(table, false, oneStats, dotOut, outputDir);
            dotOut.close();

            dotOut = new LineWriter(twoDegreesDotFile, Config.DOT_CHARSET);
            WriteStats twoStats = new WriteStats(stats);
            impliedConstraints = formatter.writeRealRelationships(table, true, twoStats, dotOut, outputDir);
            dotOut.close();

            if (oneStats.getNumTablesWritten() + oneStats.getNumViewsWritten() == twoStats.getNumTablesWritten() + twoStats.getNumViewsWritten()) {
                Files.deleteIfExists(twoDegreesDotFile.toPath()); // no different than before, so don't show it
            }

            if (!impliedConstraints.isEmpty()) {
                dotOut = new LineWriter(oneImpliedDotFile, Config.DOT_CHARSET);
                formatter.writeAllRelationships(table, false, stats, dotOut, outputDir);
                dotOut.close();

                dotOut = new LineWriter(twoImpliedDotFile, Config.DOT_CHARSET);
                formatter.writeAllRelationships(table, true, stats, dotOut, outputDir);
                dotOut.close();
                return true;
            }
        }

        return false;
    }

    private Object generateDiagrams(Table table, WriteStats stats, File outputDir, List<MustacheTableDiagram> diagrams) throws IOException {
        Object graphviz = new Object();

        File diagramsDir = new File(outputDir, "diagrams");
        generateDots(table, diagramsDir, stats, outputDir);

        if (table.getMaxChildren() + table.getMaxParents() > 0) {
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