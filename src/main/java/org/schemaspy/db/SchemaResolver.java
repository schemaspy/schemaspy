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
package org.schemaspy.db;

import org.schemaspy.model.InvalidConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Objects;

public class SchemaResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private DatabaseMetaData databaseMetaData;

    public SchemaResolver(DatabaseMetaData databaseMetaData) {
        this.databaseMetaData = databaseMetaData;
    }

    public String resolveSchema(String currentSchema) {
        if (Objects.isNull(currentSchema)) {
            String schema = null;
            try {
                schema = databaseMetaData.getConnection().getSchema();
                LOGGER.debug("Schema not provided, queried jdbc driver and got '{}'", schema);
            } catch (SQLException sqle) {
                LOGGER.error("Schema (-s/-schemas) not provided, queried jdbc driver for schema and failed", sqle);
            }

            if (Objects.isNull(schema)) {
                throw new InvalidConfigurationException("Schema (-s/-schemas) was not provided and unable to deduce schema, schema is sometimes referred to as user/owner/database");
            }
            return schema;
        } else {
            return currentSchema;
        }
    }
}
