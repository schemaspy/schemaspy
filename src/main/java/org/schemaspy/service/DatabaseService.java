/*
 * Copyright (C) 2004 - 2011, 2014 John Currier
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2017 Ismail Simsek
 * Copyright (C) 2017 Thomas Traude
 * Copyright (C) 2017 Daniel Watt
 * Copyright (C) 2017, 2018 Nils Petzaell
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
import org.schemaspy.model.*;
import org.schemaspy.model.xml.SchemaMeta;
import org.schemaspy.model.xml.TableMeta;
import org.schemaspy.service.helper.BasicTableMeta;
import org.schemaspy.validator.NameValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by rkasa on 2016-12-10.
 * @author John Currier
 * @author Rafal Kasa
 * @author Ismail Simsek
 * @author Thomas Traude
 * @author Daniel Watt
 * @author Nils Petzaell
 */
public class DatabaseService {

    private final TableService tableService;

    private final ViewService viewService;

    private final SqlService sqlService;

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public DatabaseService(TableService tableService, ViewService viewService, SqlService sqlService) {
        this.tableService = Objects.requireNonNull(tableService);
        this.viewService = Objects.requireNonNull(viewService);
        this.sqlService = Objects.requireNonNull(sqlService);
    }

    public void gatheringSchemaDetails(Config config, Database db, SchemaMeta schemaMeta, ProgressListener listener) throws SQLException {
        LOGGER.info("Gathering schema details");

        listener.startedGatheringDetails();

        DatabaseMetaData meta = sqlService.getDatabaseMetaData();

        initTables(config, db, listener, meta);
        if (config.isViewsEnabled())
            initViews(config, db, listener, meta);
        
        initCatalogs(db);
        initSchemas(db);

        initCheckConstraints(config, db, listener);
        initTableIds(config, db);
        initIndexIds(config, db);
        initTableComments(config, db, listener);
        initTableColumnComments(config, db, listener);
        initViewComments(config, db, listener);
        initViewColumnComments(config, db, listener);
        initColumnTypes(config, db, listener);
        initRoutines(config, db, listener);

        listener.startedConnectingTables();

        connectTables(db, listener);
        updateFromXmlMetadata(config, db, schemaMeta);
    }
    
    private void initCatalogs(Database db) throws SQLException {

            String sql = Config.getInstance().getDbProperties().getProperty("selectCatalogsSql");

            if (sql != null && db.getCatalog() != null) {
                try (PreparedStatement stmt = sqlService.prepareStatement(sql, db, null);
                     ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                         db.getCatalog().setComment(rs.getString("catalog_comment"));
                    }
                } catch (SQLException sqlException) {
                    LOGGER.error(sql);
                    throw sqlException;
                }
            }
    }

    private void initSchemas(Database db) throws SQLException {
    	  String sql = Config.getInstance().getDbProperties().getProperty("selectSchemasSql");

          if (sql != null &&  db.getSchema() != null) {
              try (PreparedStatement stmt = sqlService.prepareStatement(sql, db, null);
                   ResultSet rs = stmt.executeQuery()) {

                  if (rs.next()) {
                       db.getSchema().setComment(rs.getString("schema_comment"));
                  }
              } catch (SQLException sqlException) {
                  LOGGER.error(sql);
                  throw sqlException;
              }
          }
    }

    /**
     * Create/initialize any tables in the schema.

     * @param metadata
     * @throws SQLException
     */
    private void initTables(Config config, Database db, ProgressListener listener, final DatabaseMetaData metadata) throws SQLException {
        final Pattern include = config.getTableInclusions();
        final Pattern exclude = config.getTableExclusions();
        final int maxThreads = config.getMaxDbThreads();

        String[] types = getTypes(config, "tableTypes", "TABLE");
        NameValidator validator = new NameValidator("table", include, exclude, types);
        List<BasicTableMeta> entries = getBasicTableMeta(config, db, metadata, true, types);

        TableCreator creator;
        if (maxThreads == 1) {
            creator = new TableCreator();
        } else {
            // creating tables takes a LONG time (based on JProbe analysis),
            // so attempt to speed it up by doing several in parallel.
            // note that it's actually DatabaseMetaData.getIndexInfo() that's expensive

            creator = new ThreadedTableCreator(maxThreads);

            // "prime the pump" so if there's a database problem we'll probably see it now
            // and not in a secondary thread
            while (!entries.isEmpty()) {
                BasicTableMeta entry = entries.remove(0);

                if (validator.isValid(entry.getName(), entry.getType())) {
                    new TableCreator().create(db, entry, listener);
                    break;
                }
            }
        }

        // kick off the secondary threads to do the creation in parallel
        for (BasicTableMeta entry : entries) {
            if (validator.isValid(entry.getName(), entry.getType())) {
                creator.create(db, entry, listener);
            }
        }

        // wait for everyone to finish
        creator.join();
    }

    /**
     * Create/initialize any views in the schema.
     *
     * @param metadata
     * @throws SQLException
     */
    private void initViews(Config config, Database db, ProgressListener listener, DatabaseMetaData metadata) throws SQLException {
        Pattern includeTables = config.getTableInclusions();
        Pattern excludeTables = config.getTableExclusions();

        String[] types = getTypes(config, "viewTypes", "VIEW");
        NameValidator validator = new NameValidator("view", includeTables, excludeTables, types);

        for (BasicTableMeta entry : getBasicTableMeta(config, db, metadata, false, types)) {
            if (validator.isValid(entry.getName(), entry.getType())) {
                View view = new View(db, entry.getCatalog(), entry.getSchema(), entry.getName(),
                        entry.getRemarks(), entry.getViewDefinition());

                tableService.gatheringTableDetails(db, view);

                if (entry.getViewDefinition() == null) {
                    view.setViewDefinition(viewService.fetchViewDefinition(db, view));
                }

                db.getViewsMap().put(view.getName(), view);
                listener.gatheringDetailsProgressed(view);

                LOGGER.debug("Found details of view {}", view.getName());
            }
        }
    }

    /**
     * Return a database-specific array of types from the .properties file
     * with the specified property name.
     *
     * @param propName
     * @param defaultValue
     * @return
     */
    private String[] getTypes(Config config, String propName, String defaultValue) {
        String value = config.getDbProperties().getProperty(propName, defaultValue);
        List<String> types = new ArrayList<>();
        for (String type : value.split(",")) {
            type = type.trim();
            if (type.length() > 0)
                types.add(type);
        }

        return types.toArray(new String[types.size()]);
    }

    /**
     * Take the supplied XML-based metadata and update our model of the schema with it
     *
     * @param schemaMeta
     * @throws SQLException
     */
    private void updateFromXmlMetadata(Config config, Database db, SchemaMeta schemaMeta) throws SQLException {
        if (schemaMeta != null) {
            if (Objects.nonNull(schemaMeta.getComments())) {
                db.getSchema().setComment(schemaMeta.getComments());
            }

            // done in three passes:
            // 1: create any new tables
            // 2: add/mod columns
            // 3: connect

            // add the newly defined tables and columns first
            for (TableMeta tableMeta : schemaMeta.getTables()) {
                Table table;

                if (tableMeta.getRemoteSchema() != null || tableMeta.getRemoteCatalog() != null) {
                    // will add it if it doesn't already exist
                    table = tableService.addRemoteTable(db, tableMeta.getRemoteCatalog(), tableMeta.getRemoteSchema(), tableMeta.getName(), db.getSchema().getName(), true);
                } else {
                    table = db.getLocals().get(tableMeta.getName());

                    if (table == null) {
                        // new table defined only in XML metadata
                        table = new LogicalTable(db, db.getCatalog().getName(), db.getSchema().getName(), tableMeta.getName(), tableMeta.getComments());
                        db.getTablesMap().put(table.getName(), table);
                    }
                }

                table.update(tableMeta);
            }

            // then tie the tables together
            for (TableMeta tableMeta : schemaMeta.getTables()) {
                Table table;

                if (tableMeta.getRemoteCatalog() != null || tableMeta.getRemoteSchema() != null) {
                    table = db.getRemoteTablesMap().get(db.getRemoteTableKey(tableMeta.getRemoteCatalog(), tableMeta.getRemoteSchema(), tableMeta.getName()));
                } else {
                    table = db.getLocals().get(tableMeta.getName());
                }

                tableService.connect(db, table, tableMeta, db.getLocals());
            }
        }
    }

    private void connectTables(Database db, ProgressListener listener) throws SQLException {
        for (Table table : db.getTables()) {
            listener.connectingTablesProgressed(table);

            tableService.connectForeignKeys(db, table, db.getLocals());
        }

        for (Table view : db.getViews()) {
            listener.connectingTablesProgressed(view);

            tableService.connectForeignKeys(db, view, db.getLocals());
        }
    }

    /**
     * Single-threaded implementation of a class that creates tables
     */
    private class TableCreator {
        /**
         * Create a table and put it into <code>tables</code>
         */
        void create(Database db, BasicTableMeta tableMeta, ProgressListener listener) throws SQLException {
            createImpl(db, tableMeta, listener);
        }

        protected void createImpl(Database db, BasicTableMeta tableMeta, ProgressListener listener) throws SQLException {
            Table table = new Table(db, tableMeta.getCatalog(), tableMeta.getSchema(), tableMeta.getName(), tableMeta.getRemarks());
            tableService.gatheringTableDetails(db, table);

            if (tableMeta.getNumRows() != -1) {
                table.setNumRows(tableMeta.getNumRows());
            }

            if (table.getNumRows() == 0) {
                long numRows = Config.getInstance().isNumRowsEnabled() ? tableService.fetchNumRows(db, table) : -1;
                table.setNumRows(numRows);
            }

            synchronized (db.getTablesMap()) {
                db.getTablesMap().put(table.getName(), table);
            }

            listener.gatheringDetailsProgressed(table);

            LOGGER.debug("Retrieved details of {}", table.getFullName());
        }

        /**
         * Wait for all of the tables to be created.
         * By default this does nothing since this implementation isn't threaded.
         */
        void join() {
        }
    }

    /**
     * Multi-threaded implementation of a class that creates tables
     */
    private class ThreadedTableCreator extends TableCreator {
        private final Set<Thread> threads = new HashSet<>();
        private final int maxThreads;

        ThreadedTableCreator(int maxThreads) {
            this.maxThreads = maxThreads;
        }

        @Override
        void create(Database db, BasicTableMeta tableMeta, ProgressListener listener) {
            Thread runner = new Thread() {
                @Override
                public void run() {
                    try {
                        createImpl(db, tableMeta, listener);
                    } catch (SQLException exc) {
                        LOGGER.error("SQL exception",exc);
                    } finally {
                        synchronized (threads) {
                            threads.remove(this);
                            threads.notify();
                        }
                    }
                }
            };

            synchronized (threads) {
                // wait for enough 'room'
                while (threads.size() >= maxThreads) {
                    try {
                        threads.wait();
                    } catch (InterruptedException interrupted) {
                    }
                }

                threads.add(runner);
            }

            runner.start();
        }

        /**
         * Wait for all of the started threads to complete
         */
        @Override
        public void join() {
            while (true) {
                Thread thread;

                synchronized (threads) {
                    Iterator<Thread> iter = threads.iterator();
                    if (!iter.hasNext())
                        break;

                    thread = iter.next();
                }

                try {
                    thread.join();
                } catch (InterruptedException exc) {
                }
            }
        }
    }

    /**
     * Return a list of basic details of the tables in the schema.
     *
     * @param metadata
     * @param forTables true if we're getting table data, false if getting view data
     * @return
     * @throws SQLException
     */
    private List<BasicTableMeta> getBasicTableMeta(Config config,
                                                   Database db,
                                                   DatabaseMetaData metadata,
                                                   boolean forTables,
                                                   String... types) throws SQLException {
        List<BasicTableMeta> basics = getBasicTableMetaFromSql(config, db, forTables);
        if (Objects.isNull(basics)) {
            basics = getBasicTableMetaFromDatabaseMetaData(metadata, db, forTables, types);
        }
        return basics;
    }
    private List<BasicTableMeta> getBasicTableMetaFromSql(Config config, Database database, boolean forTables) {
        String queryName = forTables ? "selectTablesSql" : "selectViewsSql";
        String sql = config.getDbProperties().getProperty(queryName);

        if (sql != null) {
            String clazz = forTables ? "table" : "view";
            try (PreparedStatement stmt = sqlService.prepareStatement(sql, database, null);
                 ResultSet rs = stmt.executeQuery()) {
                List<BasicTableMeta> basics = new ArrayList<>();
                while (rs.next()) {
                    String name = rs.getString(clazz + "_name");
                    String cat = getOptionalString(rs, clazz + "_catalog");
                    String sch = getOptionalString(rs, clazz + "_schema");
                    if (cat == null && sch == null)
                        sch = database.getSchema().getName();
                    String remarks = getOptionalString(rs, clazz + "_comment");
                    String viewDefinition = forTables ? null : getOptionalString(rs, "view_definition");
                    String rows = forTables ? getOptionalString(rs, "table_rows") : null;
                    long numRows = rows == null ? -1 : Long.parseLong(rows);

                    basics.add(new BasicTableMeta(cat, sch, name, clazz, remarks, viewDefinition, numRows));
                }
                return basics;
            } catch (SQLException sqlException) {
                LOGGER.warn("Failed to retrieve '{}' names with custom SQL '{}", clazz, sql, sqlException);
            }
        }
        return null;
    }

    private List<BasicTableMeta> getBasicTableMetaFromDatabaseMetaData(DatabaseMetaData databaseMetaData, Database database, boolean forTables, String... types) throws SQLException {
        List<BasicTableMeta> basics = new ArrayList<>();
        String lastTableName = null;
        try (ResultSet rs = databaseMetaData.getTables(null, database.getSchema().getName(), "%", types)){
            while (rs.next()) {
                String name = rs.getString("TABLE_NAME");
                lastTableName = name;
                String type = rs.getString("TABLE_TYPE");
                String cat = rs.getString("TABLE_CAT");
                String schem = rs.getString("TABLE_SCHEM");
                String remarks = getOptionalString(rs, "REMARKS");

                basics.add(new BasicTableMeta(cat, schem, name, type, remarks, null, -1));
            }
        } catch (SQLException exc) {
            if (forTables)
                throw exc;
            LOGGER.warn("Ignoring view '{}' due to exception", lastTableName, exc);
        }
        return basics;
    }

    /**
     * Some databases don't play nice with their metadata.
     * E.g. Oracle doesn't have a REMARKS column at all.
     * This method ignores those types of failures, replacing them with null.
     */
    private String getOptionalString(ResultSet rs, String columnName)
    {
        try {
            return rs.getString(columnName);
        } catch (SQLException ignore) {
            return null;
        }
    }

    private void initCheckConstraints(Config config, Database db, ProgressListener listener) {
        String sql = config.getDbProperties().getProperty("selectCheckConstraintsSql");
        if (sql != null) {
            try (PreparedStatement stmt = sqlService.prepareStatement(sql, db,null);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String tableName = rs.getString("table_name");
                    Table table = db.getLocals().get(tableName);
                    if (table != null)
                        table.addCheckConstraint(rs.getString("constraint_name"), rs.getString("text"));
                }
            } catch (SQLException sqlException) {
                // don't die just because this failed
                String msg = listener.recoverableExceptionEncountered("Failed to retrieve check constraints", sqlException, sql);
                if (msg != null) {
                    LOGGER.warn(msg);
                }
            }
        }
    }

    private void initColumnTypes(Config config, Database db, ProgressListener listener) {
        String sql = config.getDbProperties().getProperty("selectColumnTypesSql");
        if (sql != null) {

            try (PreparedStatement stmt = sqlService.prepareStatement(sql, db, null);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String tableName = rs.getString("table_name");
                    Table table = db.getLocals().get(tableName);
                    if (table != null) {
                        String columnName = rs.getString("column_name");
                        TableColumn column = table.getColumn(columnName);
                        if (column != null) {
                            column.setTypeName(rs.getString("column_type"));
                            column.setShortType(getOptionalString(rs, "short_column_type"));
                        }
                    }
                }
            } catch (SQLException sqlException) {
                // don't die just because this failed
                String msg = listener.recoverableExceptionEncountered("Failed to retrieve column type details", sqlException, sql);
                if (msg != null) {
                    LOGGER.warn(msg);
                }
            }
        }
    }

    private void initTableIds(Config config, Database db) throws SQLException {
        String sql = config.getDbProperties().getProperty("selectTableIdsSql");
        if (sql != null) {

            try (PreparedStatement stmt = sqlService.prepareStatement(sql, db, null);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String tableName = rs.getString("table_name");
                    Table table = db.getLocals().get(tableName);
                    if (table != null)
                        table.setId(rs.getObject("table_id"));
                }
            } catch (SQLException sqlException) {
                System.err.println();
                System.err.println(sql);
                throw sqlException;
            }
        }
    }

    private void initIndexIds(Config config, Database db) throws SQLException {
        String sql = config.getDbProperties().getProperty("selectIndexIdsSql");
        if (sql != null) {

            try (PreparedStatement stmt = sqlService.prepareStatement(sql, db, null);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String tableName = rs.getString("table_name");
                    Table table = db.getLocals().get(tableName);
                    if (table != null) {
                        TableIndex index = table.getIndex(rs.getString("index_name"));
                        if (index != null)
                            index.setId(rs.getObject("index_id"));
                    }
                }
            } catch (SQLException sqlException) {
                System.err.println();
                System.err.println(sql);
                throw sqlException;
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
    private void initTableComments(Config config, Database db, ProgressListener listener) {
        String sql = config.getDbProperties().getProperty("selectTableCommentsSql");
        if (sql != null) {

            try (PreparedStatement stmt = sqlService.prepareStatement(sql, db, null);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String tableName = rs.getString("table_name");
                    Table table = db.getLocals().get(tableName);
                    if (table != null)
                        table.setComments(rs.getString("comments"));
                }
            } catch (SQLException sqlException) {
                // don't die just because this failed
                String msg = listener.recoverableExceptionEncountered("Failed to retrieve table/view comments", sqlException, sql);
                if (msg != null) {
                    LOGGER.warn(msg);
                }
            }
        }
    }

    /**
     * Initializes view comments.
     *
     * @throws SQLException
     */
    private void initViewComments(Config config, Database db, ProgressListener listener) {
        String sql = config.getDbProperties().getProperty("selectViewCommentsSql");
        if (sql != null) {

            try (PreparedStatement stmt = sqlService.prepareStatement(sql, db, null);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String viewName = rs.getString("view_name");
                    if (viewName == null)
                        viewName = rs.getString("table_name");
                    Table view = db.getViewsMap().get(viewName);

                    if (view != null)
                        view.setComments(rs.getString("comments"));
                }
            } catch (SQLException sqlException) {
                // don't die just because this failed
                String msg = listener.recoverableExceptionEncountered("Failed to retrieve table/view comments", sqlException, sql);
                if (msg != null) {
                    LOGGER.warn(msg);
                }
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
    private void initTableColumnComments(Config config, Database db, ProgressListener listener) {
        String sql = config.getDbProperties().getProperty("selectColumnCommentsSql");
        if (sql != null) {

            try (PreparedStatement stmt = sqlService.prepareStatement(sql, db, null);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String tableName = rs.getString("table_name");
                    Table table = db.getLocals().get(tableName);
                    if (table != null) {
                        TableColumn column = table.getColumn(rs.getString("column_name"));
                        if (column != null)
                            column.setComments(rs.getString("comments"));
                    }
                }
            } catch (SQLException sqlException) {
                // don't die just because this failed
                String msg = listener.recoverableExceptionEncountered("Failed to retrieve column comments", sqlException, sql);
                if (msg != null) {
                    LOGGER.warn(msg);
                }
            }
        }
    }

    /**
     * Initializes view column comments.
     *
     * @throws SQLException
     */
    private void initViewColumnComments(Config config, Database db, ProgressListener listener) {
        String sql = config.getDbProperties().getProperty("selectViewColumnCommentsSql");
        if (sql != null) {

            try (PreparedStatement stmt = sqlService.prepareStatement(sql, db, null);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String viewName = rs.getString("view_name");
                    if (viewName == null)
                        viewName = rs.getString("table_name");
                    Table view = db.getViewsMap().get(viewName);

                    if (view != null) {
                        TableColumn column = view.getColumn(rs.getString("column_name"));
                        if (column != null)
                            column.setComments(rs.getString("comments"));
                    }
                }
            } catch (SQLException sqlException) {
                // don't die just because this failed
                String msg = listener.recoverableExceptionEncountered("Failed to retrieve view column comments", sqlException, sql);
                if (msg != null) {
                    LOGGER.warn(msg);
                }
            }
        }
    }

    /**
     * Initializes stored procedures / functions.
     *
     * @throws SQLException
     */
    private void initRoutines(Config config, Database db, ProgressListener listener) {
        String sql = config.getDbProperties().getProperty("selectRoutinesSql");

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
                    db.getRoutinesMap().put(routineName, routine);
                }
            } catch (SQLException sqlException) {
                // don't die just because this failed
                String msg = listener.recoverableExceptionEncountered("Failed to retrieve stored procedure/function details", sqlException, sql);
                if (msg != null) {
                    LOGGER.warn(msg);
                }
            }
        }

        sql = config.getDbProperties().getProperty("selectRoutineParametersSql");

        if (sql != null) {

            try (PreparedStatement stmt = sqlService.prepareStatement(sql, db, null);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String routineName = rs.getString("specific_name");

                    Routine routine = db.getRoutinesMap().get(routineName);
                    if (routine != null) {
                        String paramName = rs.getString("parameter_name");
                        String type = rs.getString("dtd_identifier");
                        String mode = rs.getString("parameter_mode");

                        RoutineParameter param = new RoutineParameter(paramName, type, mode);
                        routine.addParameter(param);
                    }

                }
            } catch (SQLException sqlException) {
                // don't die just because this failed
                String msg = listener.recoverableExceptionEncountered("Failed to retrieve stored procedure/function details", sqlException, sql);
                if (msg != null) {
                    LOGGER.warn(msg);
                }
            }
        }
    }

}
