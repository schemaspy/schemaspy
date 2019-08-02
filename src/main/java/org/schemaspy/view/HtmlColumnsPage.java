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

import org.json.JSONArray;
import org.json.JSONObject;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.Comparator;
import java.util.stream.Collectors;

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

        Set<MustacheTableColumn> tmpColumns = tables.stream()
                .flatMap(table -> table.getColumns().stream())
                .map(tableColumn -> new MustacheTableColumn(tableColumn, indexedColumns.contains(tableColumn), mustacheCompiler.getRootPath(0)))
                .collect(Collectors.toSet());

	Set<MustacheTableColumn> tableColumns = new TreeSet<MustacheTableColumn> (new Comparator<MustacheTableColumn>() {
		@Override
		public int compare(MustacheTableColumn o1, MustacheTableColumn o2){
		    int result =  o1.getColumn().getTable().getName().compareTo(o2.getColumn().getTable().getName());

		    if (result == 0) {
		    result =  o1.getColumn().getName().compareTo(o2.getColumn().getName());
		    }
		    return result;
		}
	    });

	tableColumns.addAll(tmpColumns);

        JSONArray columns = new JSONArray();

        tableColumns.forEach(mc -> {
            columns.put(switchToLinkedHashMap(new JSONObject())
                .put("tableName", valueOrEmptyString(mc.getColumn().getTable().getName()))
                .put("tableType", valueOrEmptyString(mc.getColumn().getTable().getType()))
                .put("keyClass", valueOrEmptyString(mc.getKeyClass()))
                .put("keyTitle", valueOrEmptyString(mc.getKeyTitle()))
                .put("name", valueOrEmptyString(mc.getKeyIcon())+valueOrEmptyString(mc.getColumn().getName()))
                .put("type", valueOrEmptyString(mc.getColumn().getTypeName()))
                .put("length", mc.getColumn().getLength())
                .put("nullable", valueOrEmptyString(mc.getNullable()))
                .put("autoUpdated", valueOrEmptyString(mc.getAutoUpdated()))
                .put("defaultValue", valueOrEmptyString(mc.getDefaultValue()))
                .put("comments", valueOrEmptyString(mc.getComments())));
        });

        PageData pageData = new PageData.Builder()
                .templateName("column.html")
                .scriptName("column.js")
                .addToScope("tableData", columns.toString(4))
                .depth(0)
                .getPageData();

        try {
            mustacheCompiler.write(pageData, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to write columns page", e);
        }
    }

    private static JSONObject switchToLinkedHashMap(JSONObject jsonObject) {
        try {
            Field map = JSONObject.class.getDeclaredField("map");
            map.setAccessible(true);
            map.set(jsonObject, new LinkedHashMap<>());
            map.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private static String valueOrEmptyString(String value) {
        return Objects.nonNull(value) ? value : "" ;
    }
}
