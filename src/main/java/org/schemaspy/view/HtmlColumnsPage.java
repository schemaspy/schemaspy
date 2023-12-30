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

import java.io.IOException;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.mustachejava.util.DecoratedCollection;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class HtmlColumnsPage {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final MustacheCompiler mustacheCompiler;

    public HtmlColumnsPage(MustacheCompiler mustacheCompiler) {
        this.mustacheCompiler = mustacheCompiler;
    }

    public void write(Collection<Table> tables, Writer writer) {
        Set<TableColumn> indexedColumns = tables.stream()
                .flatMap(table -> table.getIndexes().stream())
                .flatMap(tableIndex -> tableIndex.getColumns().stream())
                .collect(Collectors.toSet());

        List<MustacheTableColumn> tableColumns = tables.stream()
            .sorted(
                Comparator.comparing(Table::getName)
            ).flatMap(table ->
                table.getColumns().stream()
            ).map(tableColumn ->
                new MustacheTableColumn(
                    tableColumn,
                    indexedColumns.contains(tableColumn),
                    mustacheCompiler.getRootPath(0)
                )
            ).toList();

        PageData pageData = new PageData.Builder()
                .templateName("column.html")
                .scriptName("column.js")
                .addToScope("columns", new DecoratedCollection<>(tableColumns))
                .depth(0)
                .getPageData();

        try {
            mustacheCompiler.write(pageData, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to write columns page", e);
        }
    }

}