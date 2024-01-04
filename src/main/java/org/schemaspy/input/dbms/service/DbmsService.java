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
package org.schemaspy.input.dbms.service;

import org.schemaspy.input.dbms.service.helper.UniformSet;
import org.schemaspy.input.dbms.service.keywords.MetadataKeywords;
import org.schemaspy.input.dbms.service.keywords.Sql92Keywords;
import org.schemaspy.model.DbmsMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DbmsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static Set<String> getSql92Keywords() {
        return Collections.unmodifiableSet(new Sql92Keywords().value());
    }

    public DbmsMeta fetchDbmsMeta(DatabaseMetaData databaseMetaData) {
        DbmsMeta.Builder builder = new DbmsMeta.Builder();

        onlyLogException(() -> builder.productName(databaseMetaData.getDatabaseProductName()));
        onlyLogException(() -> builder.productVersion(databaseMetaData.getDatabaseProductVersion()));
        onlyLogException(() -> builder.sqlKeywords(getSQLKeywords(databaseMetaData)));
        onlyLogException(() -> builder.systemFunctions(new UniformSet(databaseMetaData.getSystemFunctions().split(",")).value()));
        onlyLogException(() -> builder.stringFunctions(new UniformSet(databaseMetaData.getStringFunctions().split(",")).value()));
        onlyLogException(() -> builder.numericFunctions(new UniformSet(databaseMetaData.getNumericFunctions().split(",")).value()));
        onlyLogException(() -> builder.timeDateFunctions(new UniformSet(databaseMetaData.getTimeDateFunctions().split(",")).value()));
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
        Set<String> allSqlKeywords = new HashSet<>(new Sql92Keywords().value());
        Set<String> sqlKeywords = new MetadataKeywords(databaseMetaData).value();
        allSqlKeywords.addAll(sqlKeywords);
        return allSqlKeywords;
    }
}
