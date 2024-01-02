/*
 * Copyright (C) 2016, 2017 Rafal Kasa
 * Copyright (C) 2016 Ismail Simsek
 * Copyright (C) 2017 Thomas Traude
 * Copyright (C) 2017 Daniel Watt
 * Copyright (C) 2017, 2018 Nils Petzaell
 *
 * This file is part of SchemaSpy.
 *
 * SchemaSpy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SchemaSpy. If not, see <http://www.gnu.org/licenses/>.
 */
package org.schemaspy.view;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.schemaspy.util.DataTableConfig;
import org.schemaspy.util.naming.NameFromString;
import org.schemaspy.util.naming.SanitizedFileName;

/**
 * Created by rkasa on 2016-03-22.
 *
 * @author Rafal Kasa
 * @author Ismail Simsek
 * @author Thomas Traude
 * @author Daniel Watt
 * @author Nils Petzaell
 */
public class MustacheCompiler {

    private final String databaseName;
    private final String schemaName;
    private final HtmlConfig htmlConfig;
    private final boolean multiSchema;
    private final DataTableConfig dataTableConfig;
    private final MustacheFactory mustacheFactory;

    public MustacheCompiler(
            String databaseName,
            String schemaName, HtmlConfig htmlConfig,
            boolean multiSchema,
            DataTableConfig dataTableConfig
    ) {
        this.databaseName = databaseName;
        this.schemaName = schemaName;
        this.htmlConfig = htmlConfig;
        this.multiSchema = multiSchema;
        this.dataTableConfig = dataTableConfig;
        this.mustacheFactory = new DefaultMustacheFactory(new MustacheCustomResolver(htmlConfig.getTemplateDirectory()));
    }

    public void write(final PageData pageData, Writer writer) throws IOException {
        StringWriter result = new StringWriter();

        HashMap<String, Object> pageScope = new HashMap<>();
        pageScope.put("toFileName", (Function<String,String>) s -> new SanitizedFileName(new NameFromString(s)).value());
        pageScope.put("databaseName", databaseName);
        pageScope.put("schemaName", schemaName);
        pageScope.put("paginationEnabled", htmlConfig.isPaginationEnabled());
        pageScope.put("displayNumRows", htmlConfig.isNumRowsEnabled());
        pageScope.put("dataTableConfig", dataTableConfig.getPageScopeMap());
        pageScope.put("markup", (Function<String,String>) md -> htmlConfig.markupProcessor(md, getRootPath(pageData.getDepth())).toHtml());
        pageScope.putAll(pageData.getScope());

        Mustache mustachePage = mustacheFactory.compile(pageData.getTemplateName());
        mustachePage.execute(result, pageScope).flush();

        HashMap<String, Object> containerScope = new HashMap<>();
        containerScope.put("content", result);
        containerScope.put("pageScript", pageData.getScriptName());
        containerScope.put("rootPath", getRootPath(pageData.getDepth()));
        containerScope.put("rootPathToHome", getRootPathToHome(pageData.getDepth()));
        containerScope.putAll(pageScope);

        Mustache mustacheContainer = mustacheFactory.compile("container.html");
        mustacheContainer.execute(writer, containerScope).flush();
    }


    public String getRootPath(int depth) {
        return IntStream.range(0, depth).mapToObj(i -> "../").collect(Collectors.joining("", "", ""));
    }

    private String getRootPathToHome(int depth) {
        String path = getRootPath(depth);
        if (multiSchema) {
            path += "../";
        }
        return path;
    }

}