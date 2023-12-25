/*
 * Copyright (C) 2011 John Currier
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

import org.schemaspy.model.Routine;
import org.schemaspy.model.Type;
import org.schemaspy.util.Markdown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.function.Function;

/**
 * The page that lists all the types (types, domains, etc.)
 * in the schema.
 *
 * @author Samuel Dussault
 */
public class HtmlTypesPage {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final MustacheCompiler mustacheCompiler;

    public HtmlTypesPage(MustacheCompiler mustacheCompiler) {
        this.mustacheCompiler = mustacheCompiler;
    }

    public void write(Collection<Type> types, Writer writer) {

        PageData pageData = new PageData.Builder()
                .templateName("types.html")
                .scriptName("types.js")
                .addToScope("types", types)
                .addToScope("md2html", (Function<String,String>) md -> new Markdown(md, mustacheCompiler.getRootPath(0)).toHtml())
                .getPageData();

        try {
            mustacheCompiler.write(pageData, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to write types page", e);
        }
    }

}