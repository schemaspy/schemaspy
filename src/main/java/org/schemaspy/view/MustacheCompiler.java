/*
 * Copyright (C) 2016, 2017 Rafal Kasa
 * Copyright (C) 2016 Ismail Simsek
 * Copyright (C) 2017 Thomas Traude
 * Copyright (C) 2017 Daniel Watt
 * Copyright (C) 2017 Nils Petzaell
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

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    private final MustacheFactory mustacheFactory;
    private final boolean multipleSchemas;

    public MustacheCompiler(String databaseName, String templateDirectory, boolean isMultipleSchemas) {
        this.databaseName = databaseName;
        this.mustacheFactory = new DefaultMustacheFactory(templateDirectory);
        this.multipleSchemas = isMultipleSchemas;
    }

    public void write(PageData pageData, Writer writer) throws IOException {
        StringWriter result = new StringWriter();

        HashMap<String, Object> containerScope = new HashMap<>();

        Mustache mustachePage = mustacheFactory.compile(pageData.getTemplateName());
        mustachePage.execute(result, pageData.getScope()).flush();

        containerScope.put("databaseName", databaseName);
        containerScope.put("content", result);
        containerScope.put("pageScript",pageData.getScriptName());
        containerScope.put("rootPath", getRootPath(pageData.getDepth()));
        containerScope.put("rootPathtoHome", getRootPathToHome(pageData.getDepth()));
        containerScope.putAll(pageData.getScope());

        Mustache mustacheContainer = mustacheFactory.compile("container.html");
        mustacheContainer.execute(writer, containerScope).flush();
    }


    public String getRootPath(int depth) {
        return IntStream.range(0, depth).mapToObj(i -> "../").collect(Collectors.joining("", "", ""));
    }

    private String getRootPathToHome(int depth) {
        String path = getRootPath(depth);
        if (multipleSchemas) {
            path += "../";
        }
        return path;
    }

}