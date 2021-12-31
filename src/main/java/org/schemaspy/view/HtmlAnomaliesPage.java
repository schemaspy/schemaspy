/*
 * Copyright (C) 2004 - 2011, 2014 John Currier
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
import org.schemaspy.DbAnalyzer;
import org.schemaspy.model.ForeignKeyConstraint;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
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
 * @author Nils Petzaell
 */
public class HtmlAnomaliesPage {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final MustacheCompiler mustacheCompiler;
    private final Config config;

    public HtmlAnomaliesPage(MustacheCompiler mustacheCompiler, Config config) {
        this.mustacheCompiler = mustacheCompiler;
        this.config = config;
    }

    public void write(
            Collection<Table> tables,
            List<? extends ForeignKeyConstraint> impliedConstraints,
            Writer writer
    ) {
        List<Table> unIndexedTables = DbAnalyzer.getTablesWithoutIndexes(new HashSet<Table>(tables));
        List<ForeignKeyConstraint> impliedConstraintColumns = impliedConstraints.stream().filter(c -> !c.getChildTable().isView()).collect(Collectors.toList());
        List<Table> oneColumnTables = DbAnalyzer.getTablesWithOneColumn(tables).stream().filter(t -> !t.isView()).collect(Collectors.toList());
        List<Table> incrementingColumnNames =  DbAnalyzer.getTablesWithIncrementingColumnNames(tables).stream().filter(t -> !t.isView()).collect(Collectors.toList());
        List<TableColumn> uniqueNullables = DbAnalyzer.getDefaultNullStringColumns(new HashSet<Table>(tables));

        PageData pageData = new PageData.Builder()
                .templateName("anomalies.html")
                .scriptName("anomalies.js")
                .addToScope("impliedConstraints", impliedConstraintColumns)
                .addToScope("unIndexedTables", unIndexedTables)
                .addToScope("oneColumnTables", oneColumnTables)
                .addToScope("incrementingColumnNames", incrementingColumnNames)
                .addToScope("uniqueNullables", uniqueNullables)
                .addToScope("anomaliesPaging", !this.config.isNoAnomaliesPaging())
                .addToScope("anomaliesPageLength", this.config.getAnomaliesPageLength())
                .addToScope("anomaliesLengthChange", this.config.isAnomaliesLengthChange())
                .depth(0)
                .getPageData();

        try {
            mustacheCompiler.write(pageData, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to write anomalies page", e);
        }
    }
}
