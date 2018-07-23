/*
 * Copyright (C) 2004 - 2011 John Currier
 * Copyright (C) 2016, 2017 Rafal Kasa
 * Copyright (C) 2017 Wojciech Kasa
 * Copyright (C) 2017 Thomas Traude
 * Copyright (C) 2017, 2018 Nils Petzaell
 * Copyright (C) 2017 Daniel Watt
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
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.model.*;
import org.schemaspy.model.xml.ForeignKeyMeta;
import org.schemaspy.model.xml.TableColumnMeta;
import org.schemaspy.model.xml.TableMeta;
import org.schemaspy.util.Markdown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.sql.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Created by rkasa on 2016-12-05.
 *
 * @author John Currier
 * @author Rafal Kasa
 * @author Ismail Simsek
 * @author Wojciech Kasa
 * @author Thomas Traude
 * @author Nils Petzaell
 * @author Daniel Watt
 */
public class TableService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SqlService sqlService;

    private final CommandLineArguments commandLineArguments;

    public TableService(SqlService sqlService, CommandLineArguments commandLineArguments) {
        this.sqlService = Objects.requireNonNull(sqlService);
        this.commandLineArguments = Objects.requireNonNull(commandLineArguments);
    }

    public void gatheringTableDetails(Database db, Table table) throws SQLException {
        markDownRegistryPage(table);
        initColumns(db, table);
        initIndexes(db, table);
        initPrimaryKeys(db, table);
    }

    /**
     * @throws SQLException
     */
    private void initColumns(Database db, Table table) throws SQLException {


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

        initColumnAutoUpdate(db, table, false);
    }

    /**
     * @param forceQuotes
     * @throws SQLException
     */
    private void initColumnAutoUpdate(Database db, Table table, boolean forceQuotes) throws SQLException {

        if (table.isView() || table.isRemote())
            return;

        // we've got to get a result set with all the columns in it
        // so we can ask if the columns are auto updated
        // Ugh!!!  Should have been in DatabaseMetaData instead!!!
        StringBuilder sql = new StringBuilder("select * from ");
        sql.append(getSchemaOrCatalog(db, table, forceQuotes));

        if (forceQuotes) {
            sql.append(sqlService.quoteIdentifier(table.getName()));
        } else
            sql.append(sqlService.getQuotedIdentifier(table.getName()));

        sql.append(" where 0 = 1");

        try (PreparedStatement stmt = sqlService.getDatabaseMetaData().getConnection().prepareStatement(sql.toString());
             ResultSet rs = stmt.executeQuery()) {

            ResultSetMetaData rsMeta = rs.getMetaData();
            for (int i = rsMeta.getColumnCount(); i > 0; --i) {
                String columnName = rsMeta.getColumnName(i);
                TableColumn column = getColumn(table, columnName);
                if (Objects.isNull(column)) {
                    throw new InconsistencyException("Column information from DatabaseMetaData differs from ResultSetMetaData, expected to find column named: "+ columnName);
                }
                column.setIsAutoUpdated(rsMeta.isAutoIncrement(i));
            }
        } catch (SQLException exc) {
            if (forceQuotes) {
                if (!table.isLogical()) {
                    // don't completely choke just because we couldn't do this....
                    LOGGER.warn("Failed to determine auto increment status: {}", exc);
                    LOGGER.warn("SQL: {}", sql);
                }
            } else {
                initColumnAutoUpdate(db, table, true);
            }
        }
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

    /**
     * @param rs - from {@link DatabaseMetaData#getColumns(String, String, String, String)}
     * @throws SQLException
     */
    protected void addColumn(Table table, ResultSet rs) throws SQLException {
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
     * "Connect" all of this table's foreign keys to their referenced primary keys
     * (and, in some cases, do the reverse as well).
     *
     * @param tables
     * @throws SQLException
     */
    public void connectForeignKeys(Database db, Table table, Map<String, Table> tables) throws SQLException {
        LOGGER.trace("Connecting foreign keys to {}", table.getFullName());


        try (ResultSet rs = sqlService.getDatabaseMetaData().getImportedKeys(table.getCatalog(), table.getSchema(), table.getName())) {
            // get our foreign keys that reference other tables' primary keys
            ArrayList<ForeignKey> importedKeys = new ArrayList<>();

            while (rs.next()) {
                ForeignKey key = new ForeignKey();
                key.setFK_NAME(rs.getString("FK_NAME"));
                key.setFKCOLUMN_NAME(rs.getString("FKCOLUMN_NAME"));
                key.setPKTABLE_CAT(rs.getString("PKTABLE_CAT"));
                key.setPKTABLE_SCHEM(rs.getString("PKTABLE_SCHEM"));
                key.setPKTABLE_NAME(rs.getString("PKTABLE_NAME"));
                key.setPKCOLUMN_NAME(rs.getString("PKCOLUMN_NAME"));
                key.setUPDATE_RULE(rs.getInt("UPDATE_RULE"));
                key.setDELETE_RULE(rs.getInt("DELETE_RULE"));
                importedKeys.add(key);
            }

            for(ForeignKey importedKey : importedKeys) {
                addForeignKey(db, table, importedKey.getFK_NAME(), importedKey.getFKCOLUMN_NAME(),
                        importedKey.getPKTABLE_CAT(), importedKey.getPKTABLE_SCHEM(),
                        importedKey.getPKTABLE_NAME(), importedKey.getPKCOLUMN_NAME(),
                        importedKey.getUPDATE_RULE(), importedKey.getDELETE_RULE(),
                        tables);
            }
        }

        // also try to find all of the 'remote' tables in other schemas that
        // point to our primary keys (not necessary in the normal case
        // as we infer this from the opposite direction)
        if (table.getSchema() != null || table.getCatalog() != null) {
            try (ResultSet rs = sqlService.getDatabaseMetaData().getExportedKeys(table.getCatalog(), table.getSchema(), table.getName())) {
                // get the foreign keys that reference our primary keys
                // note that this can take an insane amount of time on Oracle (i.e. 30 secs per call)

                ArrayList<ForeignKey> exportedKeys = new ArrayList<>();

                while (rs.next()) {
                    ForeignKey key = new ForeignKey();
                    key.setFKTABLE_CAT(rs.getString("FKTABLE_CAT"));
                    key.setFKTABLE_SCHEM(rs.getString("FKTABLE_SCHEM"));
                    key.setFKTABLE_NAME(rs.getString("FKTABLE_NAME"));
                    exportedKeys.add(key);
                }

                for(ForeignKey exportedKey : exportedKeys) {
                    String otherCatalog = exportedKey.getFKTABLE_CAT();
                    String otherSchema = exportedKey.getFKTABLE_SCHEM();
                    if (!String.valueOf(table.getSchema()).equals(String.valueOf(otherSchema)) ||
                            !String.valueOf(table.getCatalog()).equals(String.valueOf(otherCatalog))) {
                        addRemoteTable(db, otherCatalog, otherSchema, exportedKey.getFKTABLE_NAME(), table.getSchema(), false);
                    }
                }
            } catch (SQLException sqlex) {
                LOGGER.warn("Failed to getExportedKeys", sqlex);
            }
        }
    }

    /**
     * Connect to the PK's referenced by this table that live in the original schema
     * @throws SQLException
     */
    private void connectForeignKeysRemoteTable(Database db, RemoteTable remoteTable, Map<String, Table> tables) throws SQLException {
        LOGGER.trace("Connecting foreign keys to {}", remoteTable.getFullName());

        try (ResultSet rs = sqlService.getDatabaseMetaData().getImportedKeys(remoteTable.getCatalog(), remoteTable.getSchema(), remoteTable.getName())){
            // get remote table's FKs that reference PKs in our schema

            while (rs.next()) {
                String otherSchema = rs.getString("PKTABLE_SCHEM");
                String otherCatalog = rs.getString("PKTABLE_CAT");

                // if it points back to our schema then use it
                if (remoteTable.getBaseContainer().equals(otherSchema) || remoteTable.getBaseContainer().equals(otherCatalog)) {
                    addForeignKey(db, remoteTable, rs.getString("FK_NAME"), rs.getString("FKCOLUMN_NAME"),
                            otherCatalog, otherSchema,
                            rs.getString("PKTABLE_NAME"), rs.getString("PKCOLUMN_NAME"),
                            rs.getInt("UPDATE_RULE"), rs.getInt("DELETE_RULE"),
                            tables);
                }
            }
        } catch (SQLException sqlExc) {
            if (!remoteTable.isLogical()) {
                // if explicitly asking for these details then propagate the exception
                if (Config.getInstance().isOneOfMultipleSchemas())
                    throw sqlExc;

                // otherwise just report the fact that we tried & couldn't
                LOGGER.warn("Couldn't resolve foreign keys for remote table '{}'", remoteTable.getFullName(), sqlExc);
            }
        }
    }

    /**
     * rs ResultSet from {@link DatabaseMetaData#getImportedKeys(String, String, String)}
     * rs.getString("FK_NAME");
     * rs.getString("FKCOLUMN_NAME");
     * rs.getString("PKTABLE_CAT");
     * rs.getString("PKTABLE_SCHEM");
     * rs.getString("PKTABLE_NAME");
     * rs.getString("PKCOLUMN_NAME");
     * @param tables Map
     * @param db
     * @throws SQLException
     */
    protected void addForeignKey(Database db, Table table, String fkName, String fkColName,
                                 String pkCatalog, String pkSchema, String pkTableName, String pkColName,
                                 int updateRule, int deleteRule,
                                 Map<String, Table> tables) throws SQLException {
        if (fkName == null)
            return;

        Pattern include = Config.getInstance().getTableInclusions();
        Pattern exclude = Config.getInstance().getTableExclusions();

        if (!include.matcher(pkTableName).matches() || exclude.matcher(pkTableName).matches()) {
            LOGGER.debug("Ignoring {} referenced by FK {}", Table.getFullName(db.getName(), pkCatalog, pkSchema, pkTableName), fkName);
            return;
        }

        ForeignKeyConstraint foreignKey = table.getForeignKeysMap().get(fkName);
        if (foreignKey == null) {
            foreignKey = new ForeignKeyConstraint(table, fkName, updateRule, deleteRule);

            table.getForeignKeysMap().put(fkName, foreignKey);
        }

        TableColumn childColumn = table.getColumn(fkColName);
        if (childColumn != null) {
            foreignKey.addChildColumn(childColumn);

            Config config = Config.getInstance();
            Table parentTable = tables.get(pkTableName);

            String parentContainer = pkSchema != null ? pkSchema : pkCatalog != null ? pkCatalog : db.getName();
            String catalog = commandLineArguments.getCatalog();
            String baseContainer = config.getSchema() != null ? config.getSchema() : catalog != null ? catalog : db.getName();

            // if named table doesn't exist in this schema
            // or exists here but really referencing same named table in another schema
            if (parentTable == null || !baseContainer.equals(parentContainer)) {
                LOGGER.debug("Adding remote table {}", Table.getFullName(db.getName(), pkCatalog, pkSchema, pkTableName));
                parentTable = addRemoteTable(db, pkCatalog, pkSchema, pkTableName, baseContainer, false);
            }

            if (parentTable != null) {
                TableColumn parentColumn = parentTable.getColumn(pkColName);
                if (parentColumn != null) {
                    foreignKey.addParentColumn(parentColumn);

                    childColumn.addParent(parentColumn, foreignKey);
                    parentColumn.addChild(childColumn, foreignKey);
                } else {
                    LOGGER.warn("Couldn't add FK '{}' to table '{}' - Column '{}' doesn't exist in table '{}'", foreignKey.getName(), table.getName(), pkColName, parentTable);
                }
            } else {
                LOGGER.warn("Couldn't add FK '{}' to table '{}' - Unknown Referenced Table '{}'", foreignKey.getName(), table.getName(), pkTableName);
            }
        } else {
            LOGGER.warn("Couldn't add FK '{}' to table '{}' - Column '{}' doesn't exist", foreignKey.getName(), table.getName(), fkColName);
        }
    }

    protected long fetchNumRows(Database db, Table table, String clause, boolean forceQuotes) throws SQLException {
        StringBuilder sql = new StringBuilder("select ");
        sql.append(clause);
        sql.append(" from ");
        sql.append(getSchemaOrCatalog(db, table, forceQuotes));

        if (forceQuotes) {
            sql.append(sqlService.quoteIdentifier(table.getName()));
        } else
            sql.append(sqlService.getQuotedIdentifier(table.getName()));

        LOGGER.trace("Fetch number of rows using sql: '{}'",sql);
        try (PreparedStatement stmt = sqlService.prepareStatement(sql.toString());
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
            return -1;
        } catch (SQLException exc) {
            if (forceQuotes) // we tried with and w/o quotes...fail this attempt
                throw exc;

            return fetchNumRows(db, table, clause, true);
        }
    }

    private String getSchemaOrCatalog(Database db, Table table, boolean forceQuotes) throws SQLException {
        String schemaOrCatalog = null;
        if (table.getSchema() != null) {
            schemaOrCatalog = table.getSchema();
        } else if (table.getCatalog() != null) {
            schemaOrCatalog = table.getCatalog();
        }
        if (schemaOrCatalog == null) {
            return "";
        }
        if (forceQuotes) {
            return sqlService.quoteIdentifier(schemaOrCatalog) + ".";
        } else {
            return sqlService.getQuotedIdentifier(schemaOrCatalog) + ".";
        }
    }

    /**
     * Fetch the number of rows contained in this table.
     *
     * returns -1 if unable to successfully fetch the row count
     *
     * @param db Database
     * @return int
     * @throws SQLException
     */
    protected long fetchNumRows(Database db, Table table) {
        if (table.isView() || table.isRemote())
            return -1;

        SQLException originalFailure = null;

        String sql = Config.getInstance().getDbProperties().getProperty("selectRowCountSql");
        if (sql != null) {

            try (PreparedStatement stmt = sqlService.prepareStatement(sql, db, table.getName());
                 ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return rs.getLong("row_count");
                }
            } catch (SQLException sqlException) {
                // don't die just because this failed
                originalFailure = sqlException;
                LOGGER.debug("Failed to fetch number of rows for '{}' using custom query: '{}'", table.getFullName(), sql, sqlException);
            }
        }

        // if we get here then we either didn't have custom SQL or it didn't work
        try {
            // '*' should work best for the majority of cases
            return fetchNumRows(db, table, "count(*)", false);
        } catch (SQLException try2Exception) {
            LOGGER.debug("Failed to fetch number of rows for '{}' using built-in query with 'count(*)'", table.getFullName(), try2Exception);
            try {
                // except nested tables...try using '1' instead
                return fetchNumRows(db, table, "count(1)", false);
            } catch (SQLException try3Exception) {
                if (!table.isLogical()) {
                    if (originalFailure != null)
                        LOGGER.warn("Failed to fetch number of rows for '{}' using custom query: '{}'", table.getFullName(), sql, originalFailure);
                    LOGGER.warn("Failed to fetch number of rows for '{}' using built-in query with 'count(*)'", table.getFullName(), try2Exception);
                    LOGGER.warn("Failed to fetch number of rows for '{}' using built-in query with 'count(1)'", table.getFullName(), try3Exception);
                }
                return -1;
            }
        }
    }

    public Table addRemoteTable(Database db, String remoteCatalog, String remoteSchema, String remoteTableName, String baseContainer, boolean logical) throws SQLException {
        String fullName = db.getRemoteTableKey(remoteCatalog, remoteSchema, remoteTableName);
        RemoteTable remoteTable = (RemoteTable)db.getRemoteTablesMap().get(fullName);
        if (remoteTable == null) {
            LOGGER.debug("Creating remote table {}", fullName);

            if (logical)
                remoteTable = new LogicalRemoteTable(db, remoteCatalog, remoteSchema, remoteTableName, baseContainer);
            else {
                remoteTable = new RemoteTable(db, remoteCatalog, remoteSchema, remoteTableName, baseContainer);
                this.initColumns(db, remoteTable);
            }

            LOGGER.debug("Adding remote table {}", fullName);

            db.getRemoteTablesMap().put(fullName, remoteTable);
            connectForeignKeysRemoteTable(db, remoteTable, db.getLocals());
        }

        return remoteTable;
    }

    /**
     * Same as {@link #connectForeignKeys(Database, Table, Map)},
     * but uses XML-based metadata
     *
     * @param tableMeta
     * @param tables
     */
    public void connect(Database db, Table table, TableMeta tableMeta, Map<String, Table> tables) {
        for (TableColumnMeta colMeta : tableMeta.getColumns()) {
            TableColumn col = table.getColumn(colMeta.getName());

            if (col != null) {
                // go thru the new foreign key defs and associate them with our columns
                for (ForeignKeyMeta fk : colMeta.getForeignKeys()) {
                    Table parent;

                    if (fk.getRemoteCatalog() != null || fk.getRemoteSchema() != null) {
                        try {
                            // adds if doesn't exist
                            parent = addRemoteTable(db, fk.getRemoteCatalog(), fk.getRemoteSchema(), fk.getTableName(), table.getContainer(), true);
                        } catch (SQLException exc) {
                            parent = null;
                            LOGGER.debug("Failed to addRemoteTable '{}.{}.{}'", fk.getRemoteCatalog(), fk.getRemoteSchema(), fk.getTableName(), exc);
                        }
                    } else {
                        parent = tables.get(fk.getTableName());
                    }

                    if (parent != null) {
                        TableColumn parentColumn = parent.getColumn(fk.getColumnName());

                        if (parentColumn == null) {
                            LOGGER.warn("Undefined column '{}.{}' referenced by '{}.{}' in XML metadata", parent.getName(), fk.getColumnName(), col.getTable(), col);
                        } else {
                            /**
                             * Merely instantiating a foreign key constraint ties it
                             * into its parent and child columns (& therefore their tables)
                             */
                            /* TODO: This sort of code suggest that too much is happening in the constructor.
                             * Should either be a factory or constructed by a method of the holding object.
                             * Or opertations preformed in the constructor should be exposed.
                             */
                            @SuppressWarnings("unused")
                            ForeignKeyConstraint unused = new ForeignKeyConstraint(parentColumn, col) {
                                @Override
                                public String getName() {
                                    return "Defined in XML";
                                }
                            };

                            // they forgot to say it was a primary key
                            if (!parentColumn.isPrimary()) {
                                LOGGER.warn("Assuming '{}.{}' is a primary key due to being referenced by '{}.{}'", parentColumn.getTable(), parentColumn, col.getTable(), col);
                                parent.setPrimaryColumn(parentColumn);
                            }
                        }
                    } else {
                        LOGGER.warn("Undefined table '{}' referenced by '{}.{}' in XML metadata", fk.getTableName(), table.getName(), col.getName());
                    }
                }
            } else {
                LOGGER.warn("Undefined column '{}.{}' in XML metadata", table.getName(), colMeta.getName());
            }
        }
    }

    /**
     * Initialize index information
     *
     * @throws SQLException
     */
    private void initIndexes(Database db, Table table) throws SQLException {
        if (table.isView() || table.isRemote())
            return;

        // first try to initialize using the index query spec'd in the .properties
        // do this first because some DB's (e.g. Oracle) do 'bad' things with getIndexInfo()
        // (they try to do a DDL analyze command that has some bad side-effects)
        if (initIndexes(db, table, Config.getInstance().getDbProperties().getProperty("selectIndexesSql")))
            return;

        // couldn't, so try the old fashioned approach


        try (ResultSet rs = sqlService.getDatabaseMetaData().getIndexInfo(table.getCatalog(), table.getSchema(), table.getName(), false, true)){

            while (rs.next()) {
                if (isIndexRow(rs))
                    addIndex(table, rs);
            }
        } catch (SQLException exc) {
            if (!table.isLogical())
                LOGGER.warn("Unable to extract index info for table '{}' in schema '{}': {}", table.getName(), table.getContainer(), exc);
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
        if (selectIndexesSql == null)
            return false;


        try (PreparedStatement stmt = sqlService.prepareStatement(selectIndexesSql, db, table.getName());
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                if (rs.getShort("TYPE") != DatabaseMetaData.tableIndexStatistic)
                    addIndex(table, rs);
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

        if (indexName == null)
            return;

        TableIndex index = table.getIndex(indexName);

        if (index == null) {
            index = new TableIndex(rs);

            table.getIndexesMap().put(index.getName(), index);
        }

        index.addColumn(table.getColumn(rs.getString("COLUMN_NAME")), rs.getString("ASC_OR_DESC"));
    }

    /**
     *
     * @throws SQLException
     */
    private void initPrimaryKeys(Database db, Table table) throws SQLException {

        LOGGER.debug("Querying primary keys for {}", table.getFullName());
        try (ResultSet rs = sqlService.getDatabaseMetaData().getPrimaryKeys(table.getCatalog(), table.getSchema(), table.getName())){
            while (rs.next())
                addPrimaryKeyColumn(table, rs);
        } catch (SQLException exc) {
            if (!table.isLogical()) {
                throw exc;
            }
        }
    }

    /**
     * @param rs
     * @throws SQLException
     */
    private static void addPrimaryKeyColumn(Table table, ResultSet rs) throws SQLException {
        String pkName = rs.getString("PK_NAME");
        if (pkName == null)
            return;

        TableIndex index = table.getIndex(pkName);
        if (index != null) {
            index.setIsPrimaryKey(true);
        }

        String columnName = rs.getString("COLUMN_NAME");

        TableColumn tableColumn = table.getColumn(columnName);
        if (Objects.nonNull(tableColumn)) {
            table.setPrimaryColumn(tableColumn);
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

    private static void markDownRegistryPage(Table table) {
        String tablePath = "tables/" + table.getName() + ".html";
        Markdown.registryPage(table.getName(), tablePath);
    }
}
