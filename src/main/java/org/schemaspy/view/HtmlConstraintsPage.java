/*
 * Copyright (C) 2004 - 2010 John Currier
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2016 Ismail Simsek
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
import org.schemaspy.model.ForeignKeyConstraint;
import org.schemaspy.model.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The page that lists all of the constraints in the schema
 *
 * @author John Currier
 * @author Rafal Kasa
 * @author Ismail Simsek
 * @author Thomas Traude
 * @author Daniel Watt
 * @author Nils Petzaell
 */
public class HtmlConstraintsPage {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final MustacheCompiler mustacheCompiler;
    private final Config config;

    public HtmlConstraintsPage(MustacheCompiler mustacheCompiler, Config config) {
        this.mustacheCompiler = mustacheCompiler;
        this.config = config;
    }

    public void write(List<ForeignKeyConstraint> constraints, Collection<Table> tables, Writer writer) {

        PageData pageData = new PageData.Builder()
                .templateName("constraint.html")
                .scriptName("constraint.js")
                .addToScope("constraints", constraints)
                .addToScope("fkPaging", !this.config.isNoFkPaging())
                .addToScope("fkPageLength", this.config.getFkPageLength())
                .addToScope("fkLengthChange", this.config.isFkLengthChange())
                .addToScope("checkPaging", !this.config.isNoCheckPaging())
                .addToScope("checkPageLength", this.config.getCheckPageLength())
                .addToScope("checkLengthChange", this.config.isCheckLengthChange())
                .addToScope("checkConstraints", collectCheckConstraints(tables))
                .depth(0)
                .getPageData();

        try {
            mustacheCompiler.write(pageData, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to write constraints page", e);
        }
    }

    private static List<MustacheCheckConstraint> collectCheckConstraints(Collection<Table> tables) {
        return tables.stream()
                .filter(table -> table.getCheckConstraints().size() > 0)
                .flatMap(table -> table.getCheckConstraints().entrySet()
                        .stream()
                        .map(entry -> new MustacheCheckConstraint(
                                table.getName(),
                                entry.getKey(),
                                entry.getValue().trim())
                        )
                ).collect(Collectors.toList());
    }
}