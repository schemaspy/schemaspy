/*
 * Copyright (C) 2017 Rafal Kasa
 * Copyright (C) 2019 Nils Petzaell
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
package org.schemaspy.input.dbms.service.helper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Rafal Kasa
 * @author Nils Petzaell
 */
public class ExportForeignKey {
    private final String fkTableCat;
    private final String fkTableSchema;
    private final String fkTableName;

    private ExportForeignKey(String fkTableCat, String fkTableSchema, String fkTableName) {
        this.fkTableCat = fkTableCat;
        this.fkTableSchema = fkTableSchema;
        this.fkTableName = fkTableName;
    }

    public String getFkTableCat() {
        return fkTableCat;
    }

    public String getFkTableSchema() {
        return fkTableSchema;
    }

    public String getFkTableName() {
        return fkTableName;
    }

    public static class Builder {
        private String fkTableCat;
        private String fkTableSchema;
        private String fkTableName;

        public Builder fromExportedKeysResultSet(ResultSet resultSet) throws SQLException {
            this.withFkTableCat(resultSet.getString("FKTABLE_CAT"))
                    .withFkTableSchema(resultSet.getString("FKTABLE_SCHEM"))
                    .withFkTableName(resultSet.getString("FKTABLE_NAME"));
            return this;
        }

        public Builder withFkTableCat(String fkTableCat) {
            this.fkTableCat = fkTableCat;
            return this;
        }

        public Builder withFkTableSchema(String fkTableSchema) {
            this.fkTableSchema = fkTableSchema;
            return this;
        }

        public Builder withFkTableName(String fkTableName) {
            this.fkTableName = fkTableName;
            return this;
        }

        public ExportForeignKey build() {
            return new ExportForeignKey(fkTableCat, fkTableSchema, fkTableName);
        }
    }
}
