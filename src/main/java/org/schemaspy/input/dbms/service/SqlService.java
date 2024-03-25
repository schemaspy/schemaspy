/*
 * Copyright (C) 2004 - 2011, 2014 John Currier
 * Copyright (C) 2016, 2017 Rafal Kasa
 * Copyright (C) 2017 Ismail Simsek
 * Copyright (C) 2017 Thomas Traude
 * Copyright (C) 2017 Nils Petzaell
 * Copyright (C) 2017 Daniel Watt
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
 * along with Foobar. If not, see <http://www.gnu.org/licenses/>.
 */
package org.schemaspy.input.dbms.service;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.schemaspy.connection.SqlConnection;
import org.schemaspy.input.dbms.service.name.DatabaseQuoted;
import org.schemaspy.input.dbms.service.name.Sanitized;
import org.schemaspy.model.Database;
import org.schemaspy.model.DbmsMeta;
import org.schemaspy.model.InvalidConfigurationException;
import org.schemaspy.util.naming.Name;
import org.schemaspy.util.naming.NameFromString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Optional.ofNullable;

/**
 * Created by rkasa on 2016-12-10.
 *
 * @author John Currier
 * @author Rafal Kasa
 * @author Ismail Simsek
 * @author Thomas Traude
 * @author Nils Petzaell
 * @author Daniel Watt
 */
public class SqlService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final DbmsService dbmsService = new DbmsService();

    private Connection connection;
    private DatabaseMetaData databaseMetaData;
    private DbmsMeta dbmsMeta;
    private Pattern invalidIdentifierPattern;

    public DatabaseMetaData connect(SqlConnection sqlConnection) throws SQLException, IOException {
        this.connection = sqlConnection.connection();
        databaseMetaData = this.connection.getMetaData();
        dbmsMeta = dbmsService.fetchDbmsMeta(databaseMetaData);
        invalidIdentifierPattern = new InvalidIdentifierPattern(databaseMetaData).pattern();
        return databaseMetaData;
    }

    public Connection getConnection() {
        return connection;
    }

    public DatabaseMetaData getDatabaseMetaData() {
        return databaseMetaData;
    }

    public DbmsMeta getDbmsMeta() {
        return dbmsMeta;
    }

    /**
     * Create a <code>PreparedStatement</code> from the specified SQL.
     * The SQL can contain these named parameters (but <b>not</b> question marks).
     * <ol>
     * <li>:schema - replaced with the name of the schema
     * <li>:owner - alias for :schema
     * <li>:table - replaced with the name of the table
     * </ol>
     *
     * @param sql       String - SQL without question marks
     * @param tableName String - <code>null</code> if the statement doesn't deal with <code>Table</code>-level details.
     * @return PreparedStatement
     * @throws SQLException
     */
    public PreparedStatement prepareStatement(String sql, Database db, String tableName) throws SQLException {
        StringBuilder sqlBuf = new StringBuilder(sql);
        List<String> sqlParams = getSqlParams(sqlBuf, db.getName(), db.getCatalog().getName(), db.getSchema().getName(), tableName); // modifies sqlBuf
        LOGGER.debug("{} {}", sqlBuf, sqlParams);

        PreparedStatement stmt = connection.prepareStatement(sqlBuf.toString());
        try {
            for (int i = 0; i < sqlParams.size(); ++i) {
                stmt.setString(i + 1, sqlParams.get(i));
            }
        } catch (SQLException exc) {
            stmt.close();
            throw exc;
        }

        return stmt;
    }

    /**
     * Replaces named parameters in <code>sql</code> with question marks and
     * returns appropriate matching values in the returned <code>List</code> of <code>String</code>s.
     *
     * @param sql       StringBuffer input SQL with named parameters, output named params are replaced with ?'s.
     * @param tableName String
     * @return List of Strings
     * @see #prepareStatement(String, Database, String)
     */
    private static List<String> getSqlParams(StringBuilder sql, String dbName, String catalog, String schema, String tableName) {
        Map<String, String> namedParams = new HashMap<>();
        if (Objects.isNull(schema)) {
            schema = dbName; // some 'schema-less' db's treat the db name like a schema (unusual case)
        }

        namedParams.put(":dbname", dbName);
        namedParams.put(":schema", schema);
        namedParams.put(":owner", schema); // alias for :schema
        if (Objects.nonNull(tableName)) {
            namedParams.put(":table", tableName);
            namedParams.put(":view", tableName); // alias for :table
        }
        if (Objects.nonNull(catalog)) {
            namedParams.put(":catalog", catalog);
        }

        List<String> sqlParams = new ArrayList<>();
        int nextColon = sql.indexOf(":");
        while (nextColon != -1) {
            String paramName = new StringTokenizer(sql.substring(nextColon), " ,\"')").nextToken();
            String paramValue = namedParams.get(paramName);
            if (Objects.isNull(paramValue)) {
                throw new InvalidConfigurationException(
                    "Unexpected named parameter '" + paramName + "' found in SQL '" + sql + "'");
            }
            sqlParams.add(paramValue);
            sql.replace(nextColon, nextColon + paramName.length(), "?"); // replace with a ?
            nextColon = sql.indexOf(":", nextColon);
        }

        return sqlParams;
    }

    public PreparedStatement prepareStatement(String sqlQuery) throws SQLException {
        return connection.prepareStatement(sqlQuery);
    }

    public String getQualifiedTableName(String catalog, String schema, String tableName, boolean forceQuotes) {
        final String maybe = ofNullable(schema).orElse(catalog);
        final String schemaOrCatalog = Objects.isNull(maybe)
            ? ""
            : getSchemaOrCatalog(maybe, forceQuotes).value() + ".";
        if (forceQuotes) {
            return schemaOrCatalog + quoteIdentifier(tableName).value();
        } else {
            return schemaOrCatalog +
                new Sanitized(
                    this.invalidIdentifierPattern,
                    this.dbmsMeta,
                    new NameFromString(tableName)
                ).value();
        }
    }

    private Name getSchemaOrCatalog(String schemaOrCatalog, boolean forceQuotes) {
        final Name result;
        if (forceQuotes) {
            result = new DatabaseQuoted(
                this.dbmsMeta,
                new NameFromString(schemaOrCatalog)
            );
        } else {
            result = new Sanitized(
                this.invalidIdentifierPattern,
                this.dbmsMeta,
                new NameFromString(schemaOrCatalog)
            );
        }
        return result;
    }

    public Name quoteIdentifier(String id) {
        return new DatabaseQuoted(
            dbmsMeta,
            new NameFromString(id)
        );
    }
}
