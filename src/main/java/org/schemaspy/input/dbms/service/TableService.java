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
package org.schemaspy.input.dbms.service;

import org.schemaspy.input.dbms.service.helper.ExportForeignKey;
import org.schemaspy.input.dbms.service.helper.ImportForeignKey;
import org.schemaspy.input.dbms.service.helper.RemoteTableIdentifier;
import org.schemaspy.input.dbms.xml.ForeignKeyMeta;
import org.schemaspy.input.dbms.xml.TableColumnMeta;
import org.schemaspy.input.dbms.xml.TableMeta;
import org.schemaspy.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static org.schemaspy.input.dbms.service.ColumnLabel.*;

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
    private final boolean exportedKeys;
    private final boolean multiSchemas;
    private final Pattern include;
    private final Pattern exclude;
    private final Properties dbProperties;
    private final ColumnService columnService;
    private final IndexService indexService;

    public TableService(
            SqlService sqlService,
            boolean exportedKeys,
            boolean multiSchemas,
            Pattern include,
            Pattern exclude,
            Properties dbProperties,
            ColumnService columnService,
            IndexService indexService
    ) {
        this.sqlService = Objects.requireNonNull(sqlService);
        this.exportedKeys = exportedKeys;
        this.multiSchemas = multiSchemas;
        this.include = include;
        this.exclude = exclude;
        this.dbProperties = dbProperties;
        this.columnService = Objects.requireNonNull(columnService);
        this.indexService = Objects.requireNonNull(indexService);
    }

    public void gatheringTableDetails(Database database, Table table) throws SQLException {
        columnService.gatherColumns(table);
        indexService.gatherIndexes(database, table);
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
            ArrayList<ImportForeignKey> importedKeys = new ArrayList<>();

            while (rs.next()) {
                importedKeys.add(new ImportForeignKey.Builder()
                        .fromImportKeysResultSet(rs)
                        .build());
            }

            for(ImportForeignKey importedKey : importedKeys) {
                addForeignKey(db, table, importedKey, tables);
            }
        } catch (SQLException sqlex) {
            LOGGER.warn("Failed to getImportedKeys", sqlex);
        }

        // also try to find all of the 'remote' tables in other schemas that
        // point to our primary keys (not necessary in the normal case
        // as we infer this from the opposite direction)
        if ((table.getSchema() != null || table.getCatalog() != null) && exportedKeys ) {
            try (ResultSet rs = sqlService.getDatabaseMetaData().getExportedKeys(table.getCatalog(), table.getSchema(), table.getName())) {
                // get the foreign keys that reference our primary keys
                // note that this can take an insane amount of time on Oracle (i.e. 30 secs per call)

                ArrayList<ExportForeignKey> exportedKeys = new ArrayList<>();

                while (rs.next()) {
                    exportedKeys.add(new ExportForeignKey.Builder()
                            .fromExportedKeysResultSet(rs)
                            .build());
                }

                for(ExportForeignKey exportedKey : exportedKeys) {
                    if (isRemote(table, exportedKey)) {
                        addRemoteTable(db, RemoteTableIdentifier.from(exportedKey), table.getSchema());
                    }
                }
            } catch (SQLException sqlex) {
                LOGGER.warn("Failed to getExportedKeys", sqlex);
            }
        }
    }

    private static boolean isRemote(Table table, ExportForeignKey foreignKey) {
        return !String.valueOf(table.getCatalog()).equals(String.valueOf(foreignKey.getFkTableCat())) ||
                !String.valueOf(table.getSchema()).equals(String.valueOf(foreignKey.getFkTableSchema()));
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
                ImportForeignKey foreignKey = new ImportForeignKey.Builder().fromImportKeysResultSet(rs).build();
                String baseContainer = remoteTable.getBaseContainer();
                String fkSchema = foreignKey.getPkTableSchema();
                String fkCatalog = foreignKey.getPkTableCat();

                // if it points back to our schema then use it
                if ((baseContainer != null) && (((fkSchema != null) && baseContainer.equals(fkSchema)) || ((fkCatalog != null) && baseContainer.equals(fkCatalog)))) {
                    addForeignKey(db, remoteTable, foreignKey, tables);
                }
            }
        } catch (SQLException sqlExc) {
            if (!remoteTable.isLogical()) {
                // if explicitly asking for these details then propagate the exception
                if (multiSchemas)
                    throw sqlExc;

                // otherwise just report the fact that we tried & couldn't
                LOGGER.warn("Couldn't resolve foreign keys for remote table '{}'", remoteTable.getFullName(), sqlExc);
            }
        }
    }

    protected void addForeignKey(Database db, Table table, ImportForeignKey foreignKey,
                                 Map<String, Table> tables) throws SQLException {
        if (Objects.isNull(foreignKey.getFkName()) || shouldExclude(db.getName(), foreignKey)) {
            return;
        }

        ForeignKeyConstraint foreignKeyConstraint = ofNullable(table.getForeignKeysMap().get(foreignKey.getFkName()))
                .orElseGet(() -> {
                    ForeignKeyConstraint fkc = new ForeignKeyConstraint(
                            table,
                            foreignKey.getFkName(),
                            foreignKey.getUpdateRule(),
                            foreignKey.getDeleteRule()
                    );
                    table.getForeignKeysMap().put(foreignKey.getFkName(), fkc);
                    return fkc;
                });

        TableColumn childColumn = table.getColumn(foreignKey.getFkColumnName());
        if (childColumn != null) {
            foreignKeyConstraint.addChildColumn(childColumn);

            Table parentTable = tables.get(foreignKey.getPkTableName());

            String parentContainer = Stream.of(foreignKey.getPkTableSchema(), foreignKey.getPkTableCat(), db.getName())
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
            String childContainer = table instanceof RemoteTable ? ((RemoteTable) table).getBaseContainer() : table.getContainer();

            // if named table doesn't exist in this schema
            // or exists here but really referencing same named table in another schema
            if (parentTable == null || !childContainer.equals(parentContainer)) {
                LOGGER.debug("Adding remote table {}", Table.getFullName(
                        db.getName(),
                        foreignKey.getPkTableCat(),
                        foreignKey.getPkTableSchema(),
                        foreignKey.getPkTableName()
                ));
                parentTable = addRemoteTable(db, RemoteTableIdentifier.from(foreignKey), table.getContainer());
            }

            TableColumn parentColumn = parentTable.getColumn(foreignKey.getPkColumnName());
            if (parentColumn != null) {
                foreignKeyConstraint.addParentColumn(parentColumn);

                childColumn.addParent(parentColumn, foreignKeyConstraint);
                parentColumn.addChild(childColumn, foreignKeyConstraint);
            } else {
                LOGGER.warn("Couldn't add FK '{}' to table '{}' - Column '{}' doesn't exist in table '{}'", foreignKeyConstraint.getName(), table.getName(), foreignKey.getPkColumnName(), parentTable);
            }
        } else {
            LOGGER.warn("Couldn't add FK '{}' to table '{}' - Column '{}' doesn't exist", foreignKeyConstraint.getName(), table.getName(), foreignKey.getFkColumnName());
        }
    }

    private boolean shouldExclude(String databaseName, ImportForeignKey foreignKey) {
        if (!include.matcher(foreignKey.getPkTableName()).matches() || exclude.matcher(foreignKey.getPkTableName()).matches()) {
            LOGGER.debug("Ignoring {} referenced by FK {}", Table.getFullName(
                    databaseName,
                    foreignKey.getPkTableCat(),
                    foreignKey.getPkTableSchema(),
                    foreignKey.getPkTableName()
            ), foreignKey.getFkName());
            return true;
        }
        return false;
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

        String sql = dbProperties.getProperty("selectRowCountSql");
        if (sql != null) {
            try {
                return fetchNumRows(db, table, sql);
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

    private long fetchNumRows(Database database, Table table, String sql) throws SQLException {
        try (PreparedStatement stmt = sqlService.prepareStatement(sql, database, table.getName());
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong("row_count");
            } else {
                throw new SQLException("Empty ResultSet");
            }
        }
    }

    protected long fetchNumRows(Database db, Table table, String clause, boolean forceQuotes) throws SQLException {
        StringBuilder sql = new StringBuilder("select ");
        sql.append(clause);
        sql.append(" from ");
        sql.append(sqlService.getQualifiedTableName(
                table.getCatalog(),
                table.getSchema(),
                table.getName(),
                forceQuotes)
        );

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

    public Table addRemoteTable(Database db, RemoteTableIdentifier remoteTableIdentifier, String schema) throws SQLException {
        return addRemoteTable(db, remoteTableIdentifier, schema, false);
    }

    public Table addLogicalRemoteTable(Database db, RemoteTableIdentifier remoteTableIdentifier, String schema) throws SQLException {
        return addRemoteTable(db, remoteTableIdentifier, schema, true);
    }

    private Table addRemoteTable(Database db, RemoteTableIdentifier remoteTableIdentifier, String baseContainer, boolean logical) throws SQLException {
        String fullName = db.getRemoteTableKey(
                remoteTableIdentifier.getCatalogName(),
                remoteTableIdentifier.getSchemaName(),
                remoteTableIdentifier.getTableName()
        );
        RemoteTable remoteTable = (RemoteTable)db.getRemoteTablesMap().get(fullName);
        if (remoteTable == null) {
            LOGGER.debug("Creating remote table {}", fullName);

            if (logical)
                remoteTable = new LogicalRemoteTable(db, remoteTableIdentifier, baseContainer);
            else {
                remoteTable = new RemoteTable(db, remoteTableIdentifier, baseContainer);
                columnService.gatherColumns(remoteTable);
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
                            parent = addLogicalRemoteTable(db, RemoteTableIdentifier.from(fk), table.getContainer());
                            addColumnIfMissing(parent, fk.getColumnName());
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
                             * Or operations preformed in the constructor should be exposed.
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

    private void addColumnIfMissing(Table parent, String columnName) {
        if (!parent.getColumnsMap().containsKey(columnName)) {
            TableColumn tableColumn = new TableColumn(parent);
            tableColumn.setName(columnName);
            parent.getColumnsMap().put(columnName, tableColumn);
        }
    }

    public void gatherTableIds(Database db) throws SQLException {
        String sql = dbProperties.getProperty("selectTableIdsSql");
        if (sql != null) {

            try (PreparedStatement stmt = sqlService.prepareStatement(sql, db, null);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String tableName = rs.getString(TABLE_NAME);
                    Table table = db.getLocals().get(tableName);
                    if (table != null)
                        table.setId(rs.getObject("table_id"));
                }
            } catch (SQLException sqlException) {
                LOGGER.warn("Failed to fetch table ids using SQL '{}'", sql, sqlException);
            }
        }
    }

    /**
     * Initializes table comments.
     * If the SQL also returns view comments then they're plugged into the
     * appropriate views.
     *
     * @throws SQLException
     */
    public void gatherTableComments(Database db) {
        String sql = dbProperties.getProperty("selectTableCommentsSql");
        if (sql != null) {

            try (PreparedStatement stmt = sqlService.prepareStatement(sql, db, null);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String tableName = rs.getString(TABLE_NAME);
                    Table table = db.getLocals().get(tableName);
                    if (table != null)
                        table.setComments(rs.getString("comments"));
                }
            } catch (SQLException sqlException) {
                // don't die just because this failed
                LOGGER.warn("Failed to retrieve table comments using SQL '{}'", sql, sqlException);
            }
        }
    }

    /**
     * Initializes table column comments.
     * If the SQL also returns view column comments then they're plugged into the
     * appropriate views.
     *
     * @throws SQLException
     */
    public void gatherTableColumnComments(Database db) {
        String sql = dbProperties.getProperty("selectColumnCommentsSql");
        if (sql != null) {

            try (PreparedStatement stmt = sqlService.prepareStatement(sql, db, null);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String tableName = rs.getString(TABLE_NAME);
                    Table table = db.getLocals().get(tableName);
                    if (table != null) {
                        TableColumn column = table.getColumn(rs.getString(COLUMN_NAME));
                        if (column != null)
                            column.setComments(rs.getString(COMMENTS));
                    }
                }
            } catch (SQLException sqlException) {
                // don't die just because this failed
                LOGGER.warn("Failed to retrieve column comments using SQL '{}'", sql, sqlException);
            }
        }
    }
}
