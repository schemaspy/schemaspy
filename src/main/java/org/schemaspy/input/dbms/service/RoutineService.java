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

import org.schemaspy.Config;
import org.schemaspy.model.Database;
import org.schemaspy.model.Routine;
import org.schemaspy.model.RoutineParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class RoutineService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SqlService sqlService;

    public RoutineService(SqlService sqlService) {
        this.sqlService = sqlService;
    }

    public void gatherRoutines(Config config, Database database) {
        initRoutines(config, database);
        initRoutineParameters(config, database);
        database.getRoutinesMap().replaceAll((routineName, routine) -> {
            Routine newRoutine = new Routine(
                    routine.getName(),
                    trim(routine.getType()),
                    trim(routine.getReturnType()),
                    trim(routine.getDefinitionLanguage()),
                    trim(routine.getDefinition()),
                    routine.isDeterministic(),
                    trim(routine.getDataAccess()),
                    trim(routine.getSecurityType()),
                    trim(routine.getComment()));
            routine.getParameters().forEach(newRoutine::addParameter);
            return newRoutine;
        });
    }

    /**
     * Initializes stored procedures / functions.
     *
     * @throws SQLException
     */
    private void initRoutines(Config config, Database db) {
        String sql = config.getDbProperties().getProperty("selectRoutinesSql");
        boolean append = Boolean.parseBoolean(config.getDbProperties().getProperty("multirowdata", "false"));
        if (sql != null) {

            try (PreparedStatement stmt = sqlService.prepareStatement(sql, db, null);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String routineName = rs.getString("routine_name");
                    String routineType = rs.getString("routine_type");
                    String returnType = rs.getString("dtd_identifier");
                    String definitionLanguage = rs.getString("routine_body");
                    String definition = rs.getString("routine_definition");
                    String dataAccess = rs.getString("sql_data_access");
                    String securityType = rs.getString("security_type");
                    boolean deterministic = rs.getBoolean("is_deterministic");
                    String comment = getOptionalString(rs, "routine_comment");
                    Routine routine = new Routine(routineName, routineType,
                            returnType, definitionLanguage, definition,
                            deterministic, dataAccess, securityType, comment);
                    if (append) {
                        db.getRoutinesMap().merge(routineName, routine, (oldRoutine, newRoutine) -> new Routine(oldRoutine.getName(), oldRoutine.getType(),
                                oldRoutine.getReturnType(), oldRoutine.getDefinitionLanguage(), oldRoutine.getDefinition() + newRoutine.getDefinition(),
                                oldRoutine.isDeterministic(), oldRoutine.getDataAccess(), oldRoutine.getSecurityType(), oldRoutine.getComment()));
                    } else {
                        db.getRoutinesMap().put(routineName, routine);
                    }
                }
            } catch (SQLException sqlException) {
                // don't die just because this failed
                LOGGER.warn("Failed to retrieve stored procedure/function details using sql '{}'", sql, sqlException);
            }
        }
    }

    private void initRoutineParameters(Config config, Database db) {
        String sql = config.getDbProperties().getProperty("selectRoutineParametersSql");

        if (sql != null) {

            try (PreparedStatement stmt = sqlService.prepareStatement(sql, db, null);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String routineName = rs.getString("specific_name");

                    Routine routine = db.getRoutinesMap().get(routineName);
                    if (routine != null) {
                        String paramName = trim(rs.getString("parameter_name"));
                        String type = trim(rs.getString("dtd_identifier"));
                        String mode = trim(rs.getString("parameter_mode"));

                        RoutineParameter param = new RoutineParameter(paramName, type, mode);
                        routine.addParameter(param);
                    }

                }
            } catch (SQLException sqlException) {
                // don't die just because this failed
                LOGGER.warn("Failed to retrieve stored procedure/function details using SQL '{}'", sql, sqlException);
            }
        }
    }

    private static String getOptionalString(ResultSet rs, String columnName) {
        try {
            return rs.getString(columnName);
        } catch (SQLException sqlException) {
            LOGGER.debug("Failed to get value for column '{}'", sqlException.getMessage(), sqlException);
            return null;
        }
    }

    private static String trim(String string) {
        if (Objects.isNull(string)) {
            return null;
        }
        return string.trim();
    }
}
