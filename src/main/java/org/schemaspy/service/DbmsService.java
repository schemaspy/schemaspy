/*
 * Copyright (C) 2018 Nils Petzaell
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
package org.schemaspy.service;

import org.schemaspy.model.DbmsMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class DbmsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final Set<String> sql92Keywords = formatSqlKeyWords(("ADA" +
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
            "| ZONE").split("[| ]"));

    public DbmsMeta fetchDbmsMeta(DatabaseMetaData databaseMetaData) throws SQLException {
        DbmsMeta.Builder builder = new DbmsMeta.Builder();

        onlyLogException(() -> builder.productName(databaseMetaData.getDatabaseProductName()));
        onlyLogException(() -> builder.productVersion(databaseMetaData.getDatabaseProductVersion()));
        onlyLogException(() -> builder.sqlKeywords(getSQLKeywords(databaseMetaData)));
        onlyLogException(() -> builder.systemFunctions(formatSqlKeyWords(databaseMetaData.getSystemFunctions().split(","))));
        onlyLogException(() -> builder.stringFunctions(formatSqlKeyWords(databaseMetaData.getStringFunctions().split(","))));
        onlyLogException(() -> builder.numericFunctions(formatSqlKeyWords(databaseMetaData.getNumericFunctions().split(","))));
        onlyLogException(() -> builder.timeDateFunctions(formatSqlKeyWords(databaseMetaData.getTimeDateFunctions().split(","))));
        onlyLogException(() -> builder.identifierQuoteString(databaseMetaData.getIdentifierQuoteString().trim()));

        return builder.getDbmsMeta();
    }

    @FunctionalInterface
    private interface MetaDataFetcher {
        void fetch() throws SQLException;
    }

    private static void onlyLogException(MetaDataFetcher fetcher) {
        try {
            fetcher.fetch();
        } catch (SQLException sqle) {
            LOGGER.warn("Failed to fetch metadata", sqle);
        }
    }

    private static Set<String> getSQLKeywords(DatabaseMetaData databaseMetaData) throws SQLException {
        Set<String> allSqlKeywords = new HashSet<>(sql92Keywords);
        String[] sqlKeywordsArray = databaseMetaData.getSQLKeywords().split(",");
        Set<String> sqlKeywords = formatSqlKeyWords(sqlKeywordsArray);
        allSqlKeywords.addAll(sqlKeywords);
        return allSqlKeywords;
    }

    private static Set<String> formatSqlKeyWords(String[] sqlKeywords) {
        return Collections.unmodifiableSet(Arrays.stream(sqlKeywords)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .collect(Collectors.toSet()));
    }
}
