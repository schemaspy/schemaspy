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
package org.schemaspy.input.dbms.service;

import java.util.Map;
import org.schemaspy.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

import static org.schemaspy.input.dbms.service.ColumnLabel.*;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SqlService sqlService;
    private final Properties dbProperties;
    private final ColumnService columnService;

    private int deprecatedNagCounter = 0;

    public ViewService(SqlService sqlService, Properties dbProperties, ColumnService columnService) {
        this.sqlService = Objects.requireNonNull(sqlService);
        this.dbProperties = Objects.requireNonNull(dbProperties);
        this.columnService = Objects.requireNonNull(columnService);
    }

    public void gatherViewsDetails(Database database, View view) throws SQLException {
        columnService.gatherColumns(view);
        if (Objects.isNull(view.getViewDefinition())) {
            gatherViewDefinition(database, view);
        }
        database.getViewsMap().put(view.getName(), view);
    }

    /**
     * Extract the SQL that describes this view from the database
     *
     * @throws SQLException if query fails
     */
    private void gatherViewDefinition(Database db, View view) throws SQLException {
        String selectViewSql = dbProperties.getProperty("selectViewSql");
        if (selectViewSql == null) {
            return;
        }

        try (PreparedStatement stmt = sqlService.prepareStatement(selectViewSql, db, view.getName());
            ResultSet resultSet = stmt.executeQuery()) {
            view.setViewDefinition(getViewDefinitionFromResultSet(resultSet));
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

    /**
     * Initializes view comments.
     */
    public void gatherViewComments(Database db) {
        String sql = dbProperties.getProperty("selectViewCommentsSql");
        if (sql != null) {
            try {
                setViewComments(sqlService.prepareStatement(sql, db, null), db.getViewsMap());
            } catch (SQLException sqlException) {
                // don't die just because this failed
                LOGGER.warn("Failed to retrieve view comments using SQL '{}'", sql, sqlException);
            }
        }
    }

    public void setViewComments(
        final PreparedStatement stmt,
        final Map<String, View> viewsMap
    ) throws SQLException {
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            Table view = new ResultSetView(viewsMap, rs).view();

            if (view != null) {
                view.setComments(rs.getString(COMMENTS));
            }
        }
    }

    /**
     * Initializes view column comments.
     */
    public void gatherViewColumnComments(Database db) {
        String sql = dbProperties.getProperty("selectViewColumnCommentsSql");
        if (sql != null) {

            try {
                setViewColumnComments(sqlService.prepareStatement(sql, db, null), db.getViewsMap());
            } catch (SQLException sqlException) {
                // don't die just because this failed
                LOGGER.warn("Failed to retrieve view column comments using SQL '{}'", sql, sqlException);
            }
        }
    }

    public void setViewColumnComments(
        final PreparedStatement stmt,
        final Map<String, View> viewsMap
    ) throws SQLException {
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            Table view = new ResultSetView(viewsMap, rs).view();
            TableColumn column = new ResultSetTableColumn(view, rs).column();
            if (column != null) {
                column.setComments(rs.getString(COMMENTS));
            }
        }
    }
}
