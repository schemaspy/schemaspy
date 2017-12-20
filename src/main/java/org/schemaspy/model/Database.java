/*
 * This file is a part of the SchemaSpy project (http://schemaspy.org).
 * Copyright (C) 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2014 John Currier
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
package org.schemaspy.model;

import org.schemaspy.Config;
import org.schemaspy.model.xml.SchemaMeta;
import org.schemaspy.util.CaseInsensitiveMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Database {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Config config;
    private final String databaseName ;
    private final Catalog catalog ;
    private final Schema schema;
    private final Map<String, Table> tables = new CaseInsensitiveMap<Table>();
    private final Map<String, View> views = new CaseInsensitiveMap<View>();
    private final Map<String, Table> remoteTables = new CaseInsensitiveMap<Table>(); // key: schema.tableName
    private final Map<String, Table> locals = new CombinedMap(tables, views);
    private final Map<String, Routine> routines = new CaseInsensitiveMap<Routine>();
    private final DatabaseMetaData meta;
    private final SchemaMeta schemaMeta;
    private final String connectTime = new SimpleDateFormat("EEE MMM dd HH:mm z yyyy").format(new Date());
    private Set<String> sqlKeywords;
    private Pattern invalidIdentifierPattern;
	private final ProgressListener listener;

    public Database(Config config, DatabaseMetaData meta, String name, String catalog, String schema, SchemaMeta schemaMeta,
    				ProgressListener progressListener) throws SQLException, MissingResourceException {
        this.config = config;
        this.meta = meta;
        this.schemaMeta = schemaMeta;
        this.databaseName = name;
        this.catalog = new Catalog(catalog);
        this.schema = new Schema(schema);
        this.listener = progressListener;
    }

    public String getName() {
        return databaseName;
    }

    public Catalog getCatalog() {
        return catalog;
    }

    public Schema getSchema() {
        return schema;
    }

    public Config getConfig()
    {
        return config;
    }

    /**
     * Details of the database type that's running under the covers.
     *
     * @return null if a description wasn't specified.
     */
    public String getDescription() {
        return config.getDescription();
    }

    public Collection<Table> getTables() {
        return tables.values();
    }

    public Map<String, Table> getTablesMap() {
        return tables;
    }

    /**
     * Return a {@link Map} of all {@link Table}s keyed by their name.
     *
     * @return
     */
    public Map<String, Table> getTablesByName() {
        return tables;
    }

    public Map<String, Table> getLocals() {
        return locals;
    }

    public Collection<View> getViews() {
        return views.values();
    }

    public Map<String, View> getViewsMap() {
        return views;
    }

    public Collection<Table> getRemoteTables() {
        return remoteTables.values();
    }

    public Map<String, Table> getRemoteTablesMap() {
        return remoteTables;
    }

    public Collection<Routine> getRoutines() {
        return routines.values();
    }

    public Map<String, Routine> getRoutinesMap() {
        return routines;
    }

    public DatabaseMetaData getMetaData() {
        return meta;
    }

    public SchemaMeta getSchemaMeta() {
        return schemaMeta;
    }

    public String getConnectTime() {
        return connectTime;
    }

    public String getDatabaseProduct() {
        try {
            return meta.getDatabaseProductName() + " - " + meta.getDatabaseProductVersion();
        } catch (SQLException exc) {
            return "";
        }
    }

    /**
     * Return an uppercased <code>Set</code> of all SQL keywords used by a database
     *
     *
     * @return
     */
    public Set<String> getSqlKeywords() {
        if (sqlKeywords == null) {
            // from http://www.contrib.andrew.cmu.edu/~shadow/sql/sql1992.txt:
            String[] sql92Keywords =
                ("ADA" +
                "| C | CATALOG_NAME | CHARACTER_SET_CATALOG | CHARACTER_SET_NAME" +
                "| CHARACTER_SET_SCHEMA | CLASS_ORIGIN | COBOL | COLLATION_CATALOG" +
                "| COLLATION_NAME | COLLATION_SCHEMA | COLUMN_NAME | COMMAND_FUNCTION | COMMITTED" +
                "| CONDITION_NUMBER | CONNECTION_NAME | CONSTRAINT_CATALOG | CONSTRAINT_NAME" +
                "| CONSTRAINT_SCHEMA | CURSOR_NAME" +
                "| DATA | DATETIME_INTERVAL_CODE | DATETIME_INTERVAL_PRECISION | DYNAMIC_FUNCTION" +
                "| FORTRAN" +
                "| LENGTH" +
                "| MESSAGE_LENGTH | MESSAGE_OCTET_LENGTH | MESSAGE_TEXT | MORE | MUMPS" +
                "| NAME | NULLABLE | NUMBER" +
                "| PASCAL | PLI" +
                "| REPEATABLE | RETURNED_LENGTH | RETURNED_OCTET_LENGTH | RETURNED_SQLSTATE" +
                "| ROW_COUNT" +
                "| SCALE | SCHEMA_NAME | SERIALIZABLE | SERVER_NAME | SUBCLASS_ORIGIN" +
                "| TABLE_NAME | TYPE" +
                "| UNCOMMITTED | UNNAMED" +
                "| ABSOLUTE | ACTION | ADD | ALL | ALLOCATE | ALTER | AND" +
                "| ANY | ARE | AS | ASC" +
                "| ASSERTION | AT | AUTHORIZATION | AVG" +
                "| BEGIN | BETWEEN | BIT | BIT_LENGTH | BOTH | BY" +
                "| CASCADE | CASCADED | CASE | CAST | CATALOG | CHAR | CHARACTER | CHAR_LENGTH" +
                "| CHARACTER_LENGTH | CHECK | CLOSE | COALESCE | COLLATE | COLLATION" +
                "| COLUMN | COMMIT | CONNECT | CONNECTION | CONSTRAINT" +
                "| CONSTRAINTS | CONTINUE" +
                "| CONVERT | CORRESPONDING | COUNT | CREATE | CROSS | CURRENT" +
                "| CURRENT_DATE | CURRENT_TIME | CURRENT_TIMESTAMP | CURRENT_USER | CURSOR" +
                "| DATE | DAY | DEALLOCATE | DEC | DECIMAL | DECLARE | DEFAULT | DEFERRABLE" +
                "| DEFERRED | DELETE | DESC | DESCRIBE | DESCRIPTOR | DIAGNOSTICS" +
                "| DISCONNECT | DISTINCT | DOMAIN | DOUBLE | DROP" +
                "| ELSE | END | END-EXEC | ESCAPE | EXCEPT | EXCEPTION" +
                "| EXEC | EXECUTE | EXISTS" +
                "| EXTERNAL | EXTRACT" +
                "| FALSE | FETCH | FIRST | FLOAT | FOR | FOREIGN | FOUND | FROM | FULL" +
                "| GET | GLOBAL | GO | GOTO | GRANT | GROUP" +
                "| HAVING | HOUR" +
                "| IDENTITY | IMMEDIATE | IN | INDICATOR | INITIALLY | INNER | INPUT" +
                "| INSENSITIVE | INSERT | INT | INTEGER | INTERSECT | INTERVAL | INTO | IS" +
                "| ISOLATION" +
                "| JOIN" +
                "| KEY" +
                "| LANGUAGE | LAST | LEADING | LEFT | LEVEL | LIKE | LOCAL | LOWER" +
                "| MATCH | MAX | MIN | MINUTE | MODULE | MONTH" +
                "| NAMES | NATIONAL | NATURAL | NCHAR | NEXT | NO | NOT | NULL" +
                "| NULLIF | NUMERIC" +
                "| OCTET_LENGTH | OF | ON | ONLY | OPEN | OPTION | OR" +
                "| ORDER | OUTER" +
                "| OUTPUT | OVERLAPS" +
                "| PAD | PARTIAL | POSITION | PRECISION | PREPARE | PRESERVE | PRIMARY" +
                "| PRIOR | PRIVILEGES | PROCEDURE | PUBLIC" +
                "| READ | REAL | REFERENCES | RELATIVE | RESTRICT | REVOKE | RIGHT" +
                "| ROLLBACK | ROWS" +
                "| SCHEMA | SCROLL | SECOND | SECTION | SELECT | SESSION | SESSION_USER | SET" +
                "| SIZE | SMALLINT | SOME | SPACE | SQL | SQLCODE | SQLERROR | SQLSTATE" +
                "| SUBSTRING | SUM | SYSTEM_USER" +
                "| TABLE | TEMPORARY | THEN | TIME | TIMESTAMP | TIMEZONE_HOUR | TIMEZONE_MINUTE" +
                "| TO | TRAILING | TRANSACTION | TRANSLATE | TRANSLATION | TRIM | TRUE" +
                "| UNION | UNIQUE | UNKNOWN | UPDATE | UPPER | USAGE | USER | USING" +
                "| VALUE | VALUES | VARCHAR | VARYING | VIEW" +
                "| WHEN | WHENEVER | WHERE | WITH | WORK | WRITE" +
                "| YEAR" +
                "| ZONE").split("[| ]+");

            String[] nonSql92Keywords = new String[0];
            try {
                nonSql92Keywords = getMetaData().getSQLKeywords().toUpperCase().split(",\\s*");
            } catch (SQLException sqle) {
                LOGGER.warn("Failed to retrieve SQLKeywords from metadata, using only SQL92 keywords");
                LOGGER.debug("Failed to retrieve SQLKeywords from metadata, using only SQL92 keywords", sqle);
            }

            sqlKeywords = new HashSet<String>() {
                private static final long serialVersionUID = 1L;

                @Override
                public boolean contains(Object key) {
                    return super.contains(((String)key).toUpperCase());
                }
            };
            sqlKeywords.addAll(Arrays.asList(sql92Keywords));
            sqlKeywords.addAll(Arrays.asList(nonSql92Keywords));
        }

        return sqlKeywords;
    }

    /**
     * Return <code>id</code> quoted if required, otherwise return <code>id</code>
     *
     * @param id
     * @return
     * @throws SQLException
     */
    public String getQuotedIdentifier(String id) throws SQLException {
        // look for any character that isn't valid (then matcher.find() returns true)
        Matcher matcher = getInvalidIdentifierPattern().matcher(id);

        boolean quotesRequired = matcher.find() || getSqlKeywords().contains(id);

        if (quotesRequired) {
            // name contains something that must be quoted
            return quoteIdentifier(id);
        }

        // no quoting necessary
        return id;
    }

    public String quoteIdentifier(String id) throws SQLException {
        String quote = getMetaData().getIdentifierQuoteString().trim();
        return quote + id + quote;
    }

    /**
     * Return a <code>Pattern</code> whose matcher will return <code>true</code>
     * when run against an identifier that contains a character that is not
     * acceptable by the database without being quoted.
     */
    private Pattern getInvalidIdentifierPattern() throws SQLException {
        if (invalidIdentifierPattern == null) {
            StringBuilder validChars = new StringBuilder("a-zA-Z0-9_");
            String reservedRegexChars = "-&^";
            String extraValidChars = getMetaData().getExtraNameCharacters();
            for (int i = 0; i < extraValidChars.length(); ++i) {
                char ch = extraValidChars.charAt(i);
                if (reservedRegexChars.indexOf(ch) >= 0)
                    validChars.append("" + "\\");
                validChars.append(ch);
            }

            invalidIdentifierPattern = Pattern.compile("[^" + validChars + "]");
        }

        return invalidIdentifierPattern;
    }

    /**
     * Returns a 'key' that's used to identify a remote table
     * in the remoteTables map.
     *
     * @param cat
     * @param sch
     * @param table
     * @return
     */
    public String getRemoteTableKey(String cat, String sch, String table) {
        return Table.getFullName(getName(), cat, sch, table);
    }

    /**
     * A read-only map that treats both collections of Tables and Views as one
     * combined collection.
     * This is a bit strange, but it simplifies logic that otherwise treats
     * the two as if they were one collection.
     */
    private class CombinedMap implements Map<String, Table> {
        private final Map<String, ? extends Table> map1;
        private final Map<String, ? extends Table> map2;

        public CombinedMap(Map<String, ? extends Table> map1, Map<String, ? extends Table> map2)
        {
            this.map1 = map1;
            this.map2 = map2;
        }

        @Override
		public Table get(Object name) {
            Table table = map1.get(name);
            if (table == null)
                table = map2.get(name);
            return table;
        }

        @Override
		public int size() {
            return map1.size() + map2.size();
        }

        @Override
		public boolean isEmpty() {
            return map1.isEmpty() && map2.isEmpty();
        }

        @Override
		public boolean containsKey(Object key) {
            return map1.containsKey(key) || map2.containsKey(key);
        }

        @Override
		public boolean containsValue(Object value) {
            return map1.containsValue(value) || map2.containsValue(value);
        }

        @Override
		public Table put(String name, Table table) {
            throw new UnsupportedOperationException();
        }

        /**
         * Warning: potentially expensive operation
         */
        @Override
		public Set<String> keySet() {
            return getCombined().keySet();
        }

        /**
         * Warning: potentially expensive operation
         */
        @Override
		public Set<Map.Entry<String, Table>> entrySet() {
            return getCombined().entrySet();
        }

        /**
         * Warning: potentially expensive operation
         */
        @Override
		public Collection<Table> values() {
            return getCombined().values();
        }

        private Map<String, Table> getCombined() {
            Map<String, Table> all = new CaseInsensitiveMap<Table>(size());
            all.putAll(map1);
            all.putAll(map2);
            return all;
        }

        @Override
		public Table remove(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
		public void putAll(Map<? extends String, ? extends Table> table) {
            throw new UnsupportedOperationException();
        }

        @Override
		public void clear() {
            throw new UnsupportedOperationException();
        }
    }
}
