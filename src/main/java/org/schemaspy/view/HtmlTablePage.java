/*
 * Copyright (C) 2004 - 2011, 2014 John Currier
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2016 Ismail Simsek
 * Copyright (C) 2017 Wojciech Kasa
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

import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.schemaspy.model.TableIndex;
import org.schemaspy.util.Markdown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.stream.Collectors;

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
public class HtmlTablePage {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final MustacheCompiler mustacheCompiler;
    private final SqlAnalyzer sqlAnalyzer;

    public HtmlTablePage(MustacheCompiler mustacheCompiler, SqlAnalyzer sqlAnalyzer) {
        this.mustacheCompiler = mustacheCompiler;
        this.sqlAnalyzer = sqlAnalyzer;
    }

    public void write(Table table, List<MustacheTableDiagram> diagrams, Writer writer) {
        Set<TableColumn> primaries = new LinkedHashSet<>(table.getPrimaryColumns());
        Set<TableColumn> indexes = new HashSet<>();
        Set<MustacheTableColumn> tableColumns = new LinkedHashSet<>();
        Set<MustacheTableIndex> indexedColumns = new LinkedHashSet<>();
        Set<MustacheCheckConstraint> checkConstraints = table.getCheckConstraints().entrySet().stream()
                .map(entry -> new MustacheCheckConstraint(table.getName(), entry.getKey(), entry.getValue()))
                .collect(Collectors.toSet());
        Set<TableIndex> sortIndexes = new TreeSet<>(table.getIndexes()); // sort primary keys first

        for (TableIndex index : sortIndexes) {
            indexes.addAll(index.getColumns());
            indexedColumns.add(new MustacheTableIndex(index));
        }

        for (TableColumn column : table.getColumns()) {
            tableColumns.add(new MustacheTableColumn(column, indexes.contains(column), mustacheCompiler.getRootPath(1)));
        }

        LOGGER.debug("Writing table page -> {}", table.getName());

        PageData pageData = new PageData.Builder()
                .templateName("tables/table.html")
                .scriptName("table.js")
                .addToScope("table", table)
                .addToScope("comments", new Markdown(table.getComments(), mustacheCompiler.getRootPath(1)).toHtml())
                .addToScope("primaries", primaries)
                .addToScope("columns", tableColumns)
                .addToScope("indexes", indexedColumns)
                .addToScope("checkConstraints", checkConstraints)
                .addToScope("diagrams", diagrams)
                .addToScope("sqlCode", sqlCode(table))
                .addToScope("references", sqlReferences(table))
                .depth(1)
                .getPageData();

        try {
            mustacheCompiler.write(pageData, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to write table page for '{}'", table.getName(), e);
        }
    }


    private Set<Table> sqlReferences(Table table) {
        Set<Table> references = new LinkedHashSet<>();

        if (table.isView() && table.getViewDefinition() != null) {
            references.addAll(sqlAnalyzer.getReferencedTables(table.getViewDefinition()));
        }
        return references;
    }

    private static String sqlCode(Table table) {
        return table.getViewDefinition() != null ? table.getViewDefinition().trim() : "";
    }

}
