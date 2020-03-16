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
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.sql.*;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ColumnService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SqlService sqlService;

    public ColumnService(SqlService sqlService) {
        this.sqlService = sqlService;
    }

    public void gatherColumns(Table table) throws SQLException {
        initColumns(table);
        if (!(table.isView() || table.isRemote())) {
            initColumnAutoUpdate(table, false);
        }
    }

    /**
     * @throws SQLException
     */
    private void initColumns(Table table) throws SQLException {
        synchronized (Table.class) {
            try (ResultSet rs = sqlService.getDatabaseMetaData().getColumns(table.getCatalog(), table.getSchema(), table.getName(), "%")) {
                while (rs.next())
                    addColumn(table, rs);
            } catch (SQLException exc) {
                if (!table.isLogical()) {
                    throw new ColumnInitializationFailure(table, exc);
                }
            }
        }
    }

    /**
     * @param rs - from {@link DatabaseMetaData#getColumns(String, String, String, String)}
     * @throws SQLException
     */
    private void addColumn(Table table, ResultSet rs) throws SQLException {
        String columnName = rs.getString("COLUMN_NAME");

        if (columnName == null)
            return;

        if (table.getColumn(columnName) == null) {
            TableColumn column = initColumn(table, rs);
            table.getColumnsMap().put(column.getName(), column);
        }
    }

    private static TableColumn initColumn(Table table, ResultSet rs) throws SQLException {
        TableColumn column = new TableColumn(table);
        // names and types are typically reused *many* times in a database,
        // so keep a single instance of each distinct one
        // (thanks to Mike Barnes for the suggestion)
        String tmp = rs.getString("COLUMN_NAME");
        column.setName(tmp == null ? null : tmp.intern());
        tmp = rs.getString("TYPE_NAME");
        column.setTypeName(tmp == null ? "unknown" : tmp.intern());
        column.setType(rs.getInt("DATA_TYPE"));

        column.setDecimalDigits(rs.getInt("DECIMAL_DIGITS"));
        Number bufLength = (Number)rs.getObject("BUFFER_LENGTH");
        if (bufLength != null && bufLength.shortValue() > 0)
            column.setLength(bufLength.shortValue());
        else
            column.setLength(rs.getInt("COLUMN_SIZE"));

        StringBuilder buf = new StringBuilder();
        buf.append(column.getLength());
        if (column.getDecimalDigits() > 0) {
            buf.append(',');
            buf.append(column.getDecimalDigits());
        }
        column.setDetailedSize(buf.toString());

        column.setNullable(rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
        column.setDefaultValue(rs.getString("COLUMN_DEF"));
        column.setComments(rs.getString("REMARKS"));
        column.setId(rs.getInt("ORDINAL_POSITION") - 1);

        Pattern excludeIndirectColumns = Config.getInstance().getIndirectColumnExclusions();
        Pattern excludeColumns = Config.getInstance().getColumnExclusions();

        column.setAllExcluded(column.matches(excludeColumns));
        column.setExcluded(column.isAllExcluded() || column.matches(excludeIndirectColumns));
        LOGGER.trace("Excluding column {}.{}: matches {}:{} {}:{}", column.getTable(), column.getName(), excludeColumns, column.isAllExcluded(), excludeIndirectColumns, column.matches(excludeIndirectColumns));

        return column;
    }

    /**
     * @param forceQuotes
     * @throws SQLException
     */
    private void initColumnAutoUpdate(Table table, boolean forceQuotes) {
        // we've got to get a result set with all the columns in it
        // so we can ask if the columns are auto updated
        // Ugh!!!  Should have been in DatabaseMetaData instead!!!
        StringBuilder sql = new StringBuilder("select * from ");
        sql.append(sqlService.getQualifiedTableName(
                table.getCatalog(),
                table.getSchema(),
                table.getName(),
                forceQuotes)
        );

        sql.append(" where 0 = 1");

        try (PreparedStatement stmt = sqlService.getDatabaseMetaData().getConnection().prepareStatement(sql.toString());
             ResultSet rs = stmt.executeQuery()) {

            ResultSetMetaData rsMeta = rs.getMetaData();
            for (int i = rsMeta.getColumnCount(); i > 0; --i) {
                String columnName = rsMeta.getColumnName(i);
                TableColumn column = getColumn(table, columnName);
                if (Objects.isNull(column)) {
                    throw new InconsistencyException("Column information from DatabaseMetaData differs from ResultSetMetaData, expected to find column named: '"+ columnName + "' in " + listColumns(table));
                }
                column.setIsAutoUpdated(rsMeta.isAutoIncrement(i));
            }
        } catch (SQLException exc) {
            if (forceQuotes) {
                if (!table.isLogical()) {
                    // don't completely choke just because we couldn't do this....
                    LOGGER.warn("Failed to determine auto increment status: {}", exc.getMessage(), exc);
                    LOGGER.warn("SQL: {}", sql);
                }
            } else {
                initColumnAutoUpdate(table, true);
            }
        }
    }

    private String listColumns(Table table) {
        return table.getColumns().stream().map(TableColumn::getName).collect(Collectors.joining("','", "['", "']"));
    }

    private static TableColumn getColumn(Table table, String columnName) {
        TableColumn column = table.getColumn(columnName);
        if (Objects.isNull(column)) {
            if (columnName.startsWith(table.getName())) {
                column = table.getColumn(columnName.substring(table.getName().length() + 1 ));
            } else if(columnName.startsWith(table.getFullName())) {
                column = table.getColumn(columnName.substring(table.getFullName().length() + 1 ));
            }
        }
        return column;
    }
}
