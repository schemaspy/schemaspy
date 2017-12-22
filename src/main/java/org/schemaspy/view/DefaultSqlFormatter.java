/*
 * This file is a part of the SchemaSpy project (http://schemaspy.org).
 * Copyright (C) 2004, 2005, 2006, 2007, 2008, 2009, 2010 John Currier
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

import java.sql.DatabaseMetaData;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.schemaspy.model.Database;
import org.schemaspy.model.Table;
import org.schemaspy.util.CaseInsensitiveMap;
import org.schemaspy.util.HtmlEncoder;

/**
 * Default implementation of {@link SqlFormatter}
 *
 * @author John Currier
 */
public class DefaultSqlFormatter implements SqlFormatter {
    private Set<String> keywords;
    private Map<String, Table> tablesByPossibleNames;
    private static String TOKENS = " \t\n\r\f()<>|,";

    /**
     * Return a HTML-formatted representation of the specified SQL.
     *
     * @param sql SQL to be formatted
     * @param db Database
     * @return HTML-formatted representation of the specified SQL
     */
    public String format(String sql, Database db, Set<Table> references) {
        StringBuilder formatted = new StringBuilder(sql.length() * 2);

        boolean alreadyFormatted = sql.contains("\n") || sql.contains("\r");
        if (alreadyFormatted)
        {
            // apparently already formatted, so dump it as is
            formatted.append("<div class='viewDefinition preFormatted'>");

            int len = sql.length();
            for (int i = 0; i < len; i++) {
                char ch = sql.charAt(i);

                // encode everything except whitespace
                if (Character.isWhitespace(ch)) {
                    formatted.append(ch);
                } else {
                    formatted.append(HtmlEncoder.encodeToken(ch));
                }
            }
        }
        else
        {
            formatted.append("  <div class='viewDefinition'>");
            @SuppressWarnings("hiding")
            Set<String> keywords = getKeywords(db.getMetaData());
            StringTokenizer tokenizer = new StringTokenizer(sql, TOKENS, true);
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                if (keywords.contains(token.toUpperCase())) {
                    formatted.append("<b>");
                    formatted.append(token);
                    formatted.append("</b>");
                } else {
                    formatted.append(HtmlEncoder.encodeToken(token));
                }
            }
        }

        formatted.append("</div>");

        references.addAll(getReferencedTables(sql, db));

        return formatted.toString();
    }

    /**
     * Returns a {@link Set} of tables/views that are possibly referenced
     * by the specified SQL.
     *
     * @param sql
     * @param db
     * @return
     */
    public Set<Table> getReferencedTables(String sql, Database db) {
        Set<Table> referenced = new HashSet<Table>();

        Map<String, Table> tables = getTableMap(db);
        @SuppressWarnings("hiding")
        Set<String> keywords = getKeywords(db.getMetaData());

        StringTokenizer tokenizer = new StringTokenizer(sql, TOKENS, true);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (!keywords.contains(token.toUpperCase())) {
                Table t = tables.get(token);

                if (t == null) {
                    int lastDot = token.lastIndexOf('.');
                    if (lastDot != -1) {
                        t = tables.get(token.substring(0, lastDot));
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
     * Returns a {@link Map} of all tables/views in the database
     * keyed by several possible ways to refer to the table.
     *
     * @param db
     * @return
     */
    protected Map<String, Table> getTableMap(Database db)
    {
        if (tablesByPossibleNames == null)
        {
            tablesByPossibleNames = new CaseInsensitiveMap<Table>();

            tablesByPossibleNames.putAll(getTableMap(db.getTables()));
            tablesByPossibleNames.putAll(getTableMap(db.getViews()));
        }

        return tablesByPossibleNames;
    }

    /**
     * Returns a {@link Map} of the specified tables/views
     * keyed by several possible ways to refer to the table.
     *
     * @param tables
     * @param dbName
     * @return
     */
    protected Map<String, Table> getTableMap(Collection<? extends Table> tables) {
        Map<String, Table> map = new CaseInsensitiveMap<Table>();
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

    /**
     * @param meta
     * @return
     */
    public Set<String> getKeywords(DatabaseMetaData meta) {
        if (keywords == null) {
            keywords = new HashSet<String>(Arrays.asList(new String[] {
                "ABSOLUTE", "ACTION", "ADD", "ALL", "ALLOCATE", "ALTER", "AND",
                "ANY", "ARE", "AS", "ASC", "ASSERTION", "AT", "AUTHORIZATION", "AVG",
                "BEGIN", "BETWEEN", "BIT", "BIT_LENGTH", "BOTH", "BY",
                "CASCADE", "CASCADED", "CASE", "CAST", "CATALOG", "CHAR", "CHARACTER",
                "CHAR_LENGTH", "CHARACTER_LENGTH", "CHECK", "CLOSE", "COALESCE",
                "COLLATE", "COLLATION", "COLUMN", "COMMIT", "CONNECT", "CONNECTION",
                "CONSTRAINT", "CONSTRAINTS", "CONTINUE", "CONVERT", "CORRESPONDING",
                "COUNT", "CREATE", "CROSS", "CURRENT", "CURRENT_DATE", "CURRENT_TIME",
                "CURRENT_TIMESTAMP", "CURRENT_USER", "CURSOR",
                "DATE", "DAY", "DEALLOCATE", "DEC", "DECIMAL", "DECLARE", "DEFAULT",
                "DEFERRABLE", "DEFERRED", "DELETE", "DESC", "DESCRIBE", "DESCRIPTOR",
                "DIAGNOSTICS", "DISCONNECT", "DISTINCT", "DOMAIN", "DOUBLE", "DROP",
                "ELSE", "END", "END - EXEC", "ESCAPE", "EXCEPT", "EXCEPTION", "EXEC",
                "EXECUTE", "EXISTS", "EXTERNAL", "EXTRACT",
                "FALSE", "FETCH", "FIRST", "FLOAT", "FOR", "FOREIGN", "FOUND", "FROM", "FULL",
                "GET", "GLOBAL", "GO", "GOTO", "GRANT", "GROUP",
                "HAVING", "HOUR",
                "IDENTITY", "IMMEDIATE", "IN", "INDICATOR", "INITIALLY", "INNER", "INPUT",
                "INSENSITIVE", "INSERT", "INT", "INTEGER", "INTERSECT", "INTERVAL", "INTO",
                "IS", "ISOLATION",
                "JOIN",
                "KEY",
                "LANGUAGE", "LAST", "LEADING", "LEFT", "LEVEL", "LIKE", "LOCAL", "LOWER",
                "MATCH", "MAX", "MIN", "MINUTE", "MODULE", "MONTH",
                "NAMES", "NATIONAL", "NATURAL", "NCHAR", "NEXT", "NO", "NOT", "NULL",
                "NULLIF", "NUMERIC",
                "OCTET_LENGTH", "OF", "ON", "ONLY", "OPEN", "OPTION", "OR", "ORDER",
                "OUTER", "OUTPUT", "OVERLAPS",
                "PAD", "PARTIAL", "POSITION", "PRECISION", "PREPARE", "PRESERVE", "PRIMARY",
                "PRIOR", "PRIVILEGES", "PROCEDURE", "PUBLIC",
                "READ", "REAL", "REFERENCES", "RELATIVE", "RESTRICT", "REVOKE", "RIGHT",
                "ROLLBACK", "ROWS",
                "SCHEMA", "SCROLL", "SECOND", "SECTION", "SELECT", "SESSION", "SESSION_USER",
                "SET", "SIZE", "SMALLINT", "SOME", "SPACE", "SQL", "SQLCODE", "SQLERROR",
                "SQLSTATE", "SUBSTRING", "SUM", "SYSTEM_USER",
                "TABLE", "TEMPORARY", "THEN", "TIME", "TIMESTAMP", "TIMEZONE_HOUR",
                "TIMEZONE_MINUTE", "TO", "TRAILING", "TRANSACTION", "TRANSLATE",
                "TRANSLATION", "TRIM", "TRUE",
                "UNION", "UNIQUE", "UNKNOWN", "UPDATE", "UPPER", "USAGE", "USER", "USING",
                "VALUE", "VALUES", "VARCHAR", "VARYING", "VIEW",
                "WHEN", "WHENEVER", "WHERE", "WITH", "WORK", "WRITE",
                "YEAR",
                "ZONE"
            }));

            try {
                String keywordsArray[] = new String[] {
                    meta.getSQLKeywords(),
                    meta.getSystemFunctions(),
                    meta.getNumericFunctions(),
                    meta.getStringFunctions(),
                    meta.getTimeDateFunctions()
                };
                for (String aKeywordsArray : keywordsArray) {
                    StringTokenizer tokenizer = new StringTokenizer(aKeywordsArray.toUpperCase(), ",");

                    while (tokenizer.hasMoreTokens()) {
                        keywords.add(tokenizer.nextToken().trim());
                    }
                }
            } catch (Exception exc) {
                // don't totally fail just because we can't extract these details...
                System.err.println(exc);
            }
        }

        return keywords;
    }
}
