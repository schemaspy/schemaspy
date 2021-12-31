/*
 * Copyright (C) 2004 - 2011 John Currier
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2016, 2017 Ismail Simsek
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * The page that contains links to the various schemas that were analyzed
 *
 * @author John Currier
 * @author Rafal Kasa
 * @author Ismail Simsek
 * @author Nils Petzaell
 */
public class HtmlMultipleSchemasIndexPage {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final MustacheCompiler mustacheCompiler;
    private final Config config;

    public HtmlMultipleSchemasIndexPage(MustacheCompiler mustacheCompiler, Config config) {
        this.mustacheCompiler = mustacheCompiler;
        this.config = config;
    }

    public void write(MustacheCatalog catalog, List<MustacheSchema> schemas, String description, String productName, Writer writer) {

        String connectTime = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("EEE MMM dd HH:mm z yyyy"));

        PageData pageData = new PageData.Builder()
                .templateName("multi.html")
                .addToScope("description", description)
                .addToScope("connectTime", connectTime)
                .addToScope("databaseProduct", productName)
                .addToScope("schemas", schemas)
                .addToScope("catalog", catalog)
                .addToScope("schemasNumber", Integer.toString(schemas.size()))
                .addToScope("isMultipleSchemas", true)
                .addToScope("dbObjectPaging", !this.config.isNoDbObjectPaging())
                .addToScope("dbObjectPageLength", this.config.getDbObjectPageLength())
                .addToScope("dbObjectLengthChange", this.config.isDbObjectLengthChange())
                .getPageData();

        try {
            mustacheCompiler.write(pageData, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to write multischema index", e);
        }
    }
}
