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
package org.schemaspy.model;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Nils Petzaell
 */
public class DbmsMeta {

    private String productName;
    private String productVersion;
    private String identifierQuoteString;
    private Set<String> sqlKeywords;
    private Set<String> systemFunctions;
    private Set<String> numericFunctions;
    private Set<String> stringFunctions;
    private Set<String> timeDateFunctions;

    private DbmsMeta() {}

    public String getProductName() {
        return productName;
    }

    public String getProductVersion() {
        return productVersion;
    }

    public String getIdentifierQuoteString() {
        return identifierQuoteString;
    }

    public Set<String> getSQLKeywords() {
        return sqlKeywords;
    }

    public Set<String> getSystemFunctions() {
        return systemFunctions;
    }

    public Set<String> getNumericFunctions() {
        return numericFunctions;
    }

    public Set<String> getStringFunctions() {
        return stringFunctions;
    }

    public Set<String> getTimeDateFunctions() {
        return timeDateFunctions;
    }

    public Set<String> getAllKeywords() {
        Set<String> keywords = new HashSet<>();
        keywords.addAll(getSQLKeywords());
        keywords.addAll(getNumericFunctions());
        keywords.addAll(getStringFunctions());
        keywords.addAll(getSystemFunctions());
        keywords.addAll(getTimeDateFunctions());
        return keywords;
    }

    public static class Builder {
        private DbmsMeta dbmsMeta = new DbmsMeta();

        public Builder productName(String productName) {
            dbmsMeta.productName = productName;
            return this;
        }

        public Builder productVersion(String productVersion) {
            dbmsMeta.productVersion = productVersion;
            return this;
        }

        public Builder identifierQuoteString(String identifierQuoteString) {
            dbmsMeta.identifierQuoteString = identifierQuoteString;
            return this;
        }

        public Builder sqlKeywords(Set<String> sqlKeywords) {
            dbmsMeta.sqlKeywords = sqlKeywords;
            return this;
        }

        public Builder systemFunctions(Set<String> systemFunctions) {
            dbmsMeta.systemFunctions = systemFunctions;
            return this;
        }

        public Builder numericFunctions(Set<String> numericFunctions) {
            dbmsMeta.numericFunctions = numericFunctions;
            return this;
        }

        public Builder stringFunctions(Set<String> stringFunctions) {
            dbmsMeta.stringFunctions = stringFunctions;
            return this;
        }

        public Builder timeDateFunctions(Set<String> timeDateFunctions) {
            dbmsMeta.timeDateFunctions = timeDateFunctions;
            return this;
        }

        public DbmsMeta getDbmsMeta() {
            return dbmsMeta;
        }
    }
}
