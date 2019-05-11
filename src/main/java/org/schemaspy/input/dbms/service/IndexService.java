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
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.schemaspy.model.TableIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

import static java.util.Optional.ofNullable;

public class IndexService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SqlService sqlService;

    public IndexService(SqlService sqlService) {
        this.sqlService = sqlService;
    }

    public void gatherIndexes(Database database, Table table) throws SQLException {
        initIndexes(database, table);
        initPrimaryKeys(database, table);
    }

    /**
     * Initialize index information
     *
     * @throws SQLException
     */
    private void initIndexes(Database db, Table table) throws SQLException {
        if (table.isView() || table.isRemote()) {
            return;
        }

        // first try to initialize using the index query spec'd in the .properties
        // do this first because some DB's (e.g. Oracle) do 'bad' things with getIndexInfo()
        // (they try to do a DDL analyze command that has some bad side-effects)
        if (initIndexes(db, table, Config.getInstance().getDbProperties().getProperty("selectIndexesSql"))) {
            return;
        }

        // couldn't, so try the old fashioned approach
        try (ResultSet rs = sqlService.getDatabaseMetaData().getIndexInfo(table.getCatalog(), table.getSchema(), table.getName(), false, true)) {
            while (rs.next()) {
                if (isIndexRow(rs)) {
                    addIndex(table, rs);
                }
            }
        } catch (SQLException exc) {
            if (!table.isLogical()) {
                LOGGER.warn("Unable to extract index info for table '{}' in schema '{}': {}", table.getName(), table.getContainer(), exc);
            }
        }
    }

    //This is to handle a problem with informix and lvarchar Issue 215. It's been reported to IBM.
    //According to DatabaseMetaData.getIndexInfo() ORDINAL_POSITION is zero when type is tableIndexStatistic.
    //Problem with informix is that lvarchar is reported back as TYPE = tableIndexOther and ORDINAL_POSITION = 0.
    private static boolean isIndexRow(ResultSet rs) throws SQLException {
        return rs.getShort("TYPE") != DatabaseMetaData.tableIndexStatistic && rs.getShort("ORDINAL_POSITION") > 0;
    }

    /**
     * Try to initialize index information based on the specified SQL
     *
     * @return boolean <code>true</code> if it worked, otherwise <code>false</code>
     */
    private boolean initIndexes(Database db, Table table, String selectIndexesSql) {
        if (selectIndexesSql == null) {
            return false;
        }

        try (ResultSet rs = sqlService.prepareStatement(selectIndexesSql, db, table.getName()).executeQuery()) {
            while (rs.next()) {
                if (rs.getShort("TYPE") != DatabaseMetaData.tableIndexStatistic) {
                    addIndex(table, rs);
                }
            }
        } catch (SQLException sqlException) {
            LOGGER.warn("Failed to query index information with SQL: {}", selectIndexesSql, sqlException);
            return false;
        }

        return true;
    }

    /**
     * @param rs
     * @throws SQLException
     */
    private static void addIndex(Table table, ResultSet rs) throws SQLException {
        String indexName = rs.getString("INDEX_NAME");

        if (indexName == null) {
            return;
        }

        TableIndex index = table.getIndex(indexName);

        if (index == null) {
            index = new TableIndex(indexName, !rs.getBoolean("NON_UNIQUE"));
            table.getIndexesMap().put(index.getName(), index);
        }

        index.addColumn(table.getColumn(rs.getString("COLUMN_NAME")), rs.getString("ASC_OR_DESC"));
    }

    /**
     *
     * @throws SQLException
     */
    private void initPrimaryKeys(Database database, Table table) throws SQLException {

        LOGGER.debug("Querying primary keys for {}", table.getFullName());
        try (ResultSet rs = getPrimaryKeys(database, table)){
            while (rs.next()) {
                addPrimaryKeyColumn(table, rs);
            }
        } catch (SQLException exc) {
            if (!table.isLogical()) {
                throw exc;
            }
        }
    }

    private ResultSet getPrimaryKeys(Database database, Table table) throws SQLException {
        String sql = Config.getInstance().getDbProperties().getProperty("selectPrimaryKeysSql");
        if (Objects.nonNull(sql)) {
            return sqlService.prepareStatement(sql, database, table.getName()).executeQuery();
        }
        return sqlService.getDatabaseMetaData().getPrimaryKeys(table.getCatalog(), table.getSchema(), table.getName());
    }

    /**
     * @param rs
     * @throws SQLException
     */
    private static void addPrimaryKeyColumn(Table table, ResultSet rs) throws SQLException {
        String pkName = rs.getString("PK_NAME");
        String columnName = rs.getString("COLUMN_NAME");
        TableColumn tableColumn = table.getColumn(columnName);
        if (Objects.nonNull(tableColumn)) {
            table.setPrimaryColumn(tableColumn);
            updateIndex(pkName, table, tableColumn);
        } else {
            LOGGER.error(
                    "Found PrimaryKey index '{}' with column '{}.{}.{}.{}'" +
                            ", but was unable to find column in table '{}'",
                    pkName,
                    rs.getString("TABLE_CAT"),
                    rs.getString("TABLE_SCHEM"),
                    rs.getString("TABLE_NAME"),
                    columnName,
                    table.getFullName()
            );
        }
    }

    private static void updateIndex(String pkName, Table table, TableColumn tableColumn) {
        if (Objects.nonNull(pkName)) {
            TableIndex tableIndex = table.getIndex(pkName);
            if(Objects.nonNull(tableIndex)) {
                tableIndex.setIsPrimaryKey(true);
            } else {
                LOGGER.warn("Found PK for table '{}' with pk name '{}', but index hasn't been found", table.getName(), pkName);
            }
        } else {
            String syntheticName = table.getName() + "_s_pk";
            TableIndex tableIndex = ofNullable(table.getIndex(syntheticName)).orElseGet(
                    () -> {
                        LOGGER.info("Found PK without index name created index '{}' for table '{}'", syntheticName, table.getName());
                        TableIndex syntheticTableIndex = new TableIndex(syntheticName, true);
                        table.getIndexesMap().put(syntheticTableIndex.getName(), syntheticTableIndex);
                        return syntheticTableIndex;
                    });
            tableIndex.addColumn(tableColumn, null);
            tableIndex.setIsPrimaryKey(true);
            LOGGER.debug("Found PK without index name, added column '{}' to index '{}' in table '{}'", tableColumn.getName(), syntheticName, table.getName());
        }
    }
}
