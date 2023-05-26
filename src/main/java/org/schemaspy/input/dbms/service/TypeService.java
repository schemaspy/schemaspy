/*
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
 * along with SchemaSpy. If not, see <http://www.gnu.org/licenses/>.
 */
package org.schemaspy.input.dbms.service;

import org.schemaspy.model.Database;
import org.schemaspy.model.Routine;
import org.schemaspy.model.RoutineParameter;
import org.schemaspy.model.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

public class TypeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SqlService sqlService;
    private final Properties dbProperties;

    public TypeService(SqlService sqlService, Properties dbProperties) {
        this.sqlService = sqlService;
        this.dbProperties = dbProperties;
    }

    public void gatherTypes(Database database) {
        String sql = dbProperties.getProperty("selectTypesSql");
        boolean append = Boolean.parseBoolean(dbProperties.getProperty("multirowdata", "false"));
        if (sql != null) {
            try (PreparedStatement stmt = sqlService.prepareStatement(sql, database, null);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String typeType = rs.getString("type_of_type");
                    String schema = rs.getString("schema");
                    String catalog = rs.getString("catalog");
                    String name = rs.getString("name");
                    String description = rs.getString("description");
                    String definition = rs.getString("definition");

                    Type type = new Type(typeType, catalog, schema, name, description, definition);
                    if (append) {
                        database.getTypesMap().merge(name, type, (oldType, newType) ->
                                new Type(
                                        oldType.getTypeOfType(),
                                        oldType.getCatalog(),
                                        oldType.getSchema(),
                                        oldType.getName(),
                                        oldType.getDescription(),
                                        oldType.getDefinition() + newType.getDefinition()));
                    }
                    else {
                        database.getTypesMap().put(name, type);
                    }
                }
            } catch (SQLException sqlException) {
                // don't die just because this failed
                LOGGER.warn("Failed to retrieve types details using sql '{}'", sql, sqlException);
            }
        }
    }
}
