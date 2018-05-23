/*
 * Copyright (C) 2004 - 2011 John Currier
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2017 Thomas Traude
 * Copyright (C) 2017 Daniel Watt
 * Copyright (C) 2017 Nils Petzaell
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

import org.schemaspy.Config;
import org.schemaspy.model.Database;
import org.schemaspy.model.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Created by rkasa on 2016-12-10.
 *
 * @author John Currier
 * @author Rafal Kasa
 * @author Ismail Simsek
 * @author Thomas Traude
 * @author Daniel Watt
 * @author Nils Petzaell
 */
public class ViewService {

    private final SqlService sqlService;

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private int deprecatedNagCounter = 0;

    public ViewService(SqlService sqlService) {
        this.sqlService = Objects.requireNonNull(sqlService);
    }

    /**
     * Extract the SQL that describes this view from the database
     *
     * @return
     * @throws SQLException
     */
    public String fetchViewDefinition(Database db, View view) throws SQLException {
        String selectViewSql = Config.getInstance().getDbProperties().getProperty("selectViewSql");
        if (selectViewSql == null) {
            return null;
        }

        try (PreparedStatement stmt = sqlService.prepareStatement(selectViewSql, db, view.getName());
            ResultSet resultSet = stmt.executeQuery()) {
            return getViewDefinitionFromResultSet(resultSet);
        } catch (SQLException sqlException) {
            LOGGER.error(selectViewSql);
            throw sqlException;
        }
    }

    private String getViewDefinitionFromResultSet(ResultSet resultSet) throws SQLException {
        if (isViewDefinitionColumnPresent(resultSet.getMetaData())) {
            return getFromViewDefinitionColumn(resultSet);
        }
        return getFromTextColumn(resultSet);
    }

    private boolean isViewDefinitionColumnPresent(ResultSetMetaData resultSetMetaData) throws SQLException {
        for(int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
            if ("view_definition".equalsIgnoreCase(resultSetMetaData.getColumnLabel(i))){
                return true;
            }
        }
        return false;
    }

    private String getFromViewDefinitionColumn(ResultSet resultSet) throws SQLException {
        StringBuilder viewDefinition = new StringBuilder();
        while (resultSet.next()) {
            viewDefinition.append(resultSet.getString("view_definition"));
        }
        return viewDefinition.toString();
    }

    private String getFromTextColumn(ResultSet resultSet) throws SQLException {
        StringBuilder viewDefinition = new StringBuilder();
        if (deprecatedNagCounter < 10) {
            LOGGER.warn("ColumnLabel 'text' has been deprecated and will be removed");
            deprecatedNagCounter++;
        }
        while (resultSet.next()) {
            viewDefinition.append(resultSet.getString("text"));
        }
        return viewDefinition.toString();
    }
}
