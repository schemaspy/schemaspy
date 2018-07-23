/*
 * Copyright (C) 2004 - 2010 John Currier
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

import org.schemaspy.model.Table;
import org.schemaspy.model.View;
import org.schemaspy.util.CaseInsensitiveMap;

import java.util.*;

/**
 *
 * @author John Currier
 * @author Daniel Watt
 * @author Nils Petzaell
 */
public class SqlAnalyzer {
    private Set<String> keywords;
    private Map<String, Table> tablesByPossibleNames;
    private static final String TOKENS = " \t\n\r\f()<>|,";

    public SqlAnalyzer(Set<String> keywords, Collection<Table> tables, Collection<View> views) {
        this.keywords = keywords;
        tablesByPossibleNames = new CaseInsensitiveMap<>();
        tablesByPossibleNames.putAll(getTableMap(tables));
        tablesByPossibleNames.putAll(getTableMap(views));
    }

    /**
     * Returns a {@link Set} of tables/views that are possibly referenced
     * by the specified SQL.
     *
     * @param sql
     * @return
     */
    public Set<Table> getReferencedTables(String sql) {
        Set<Table> referenced = new HashSet<>();

        StringTokenizer tokenizer = new StringTokenizer(sql, TOKENS, true);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (!keywords.contains(token.toUpperCase())) {
                Table t = tablesByPossibleNames.get(token);

                if (t == null) {
                    int lastDot = token.lastIndexOf('.');
                    if (lastDot != -1) {
                        t = tablesByPossibleNames.get(token.substring(0, lastDot));
                    }
                }

                if (t != null) {
                    referenced.add(t);
                }
            }
        }

        return referenced;
    }

    /**
     * Returns a {@link Map} of the specified tables/views
     * keyed by several possible ways to refer to the table.
     *
     * @param tables
     * @return
     */
    private static Map<String, Table> getTableMap(Collection<? extends Table> tables) {
        Map<String, Table> map = new CaseInsensitiveMap<>();
        for (Table t : tables) {
            String name = t.getName();
            String container = t.getContainer();

            map.put(name, t);
            map.put("`" + name + "`", t);
            map.put("'" + name + "'", t);
            map.put("\"" + name + "\"", t);
            map.put(container + "." + name, t);
            map.put("`" + container + "`.`" + name + "`", t);
            map.put("'" + container + "'.'" + name + "'", t);
            map.put("\"" + container + "\".\"" + name + "\"", t);
            map.put("`" + container + '.' + name + "`", t);
            map.put("'" + container + '.' + name + "'", t);
            map.put("\"" + container + '.' + name + "\"", t);
        }

        return map;
    }
}
