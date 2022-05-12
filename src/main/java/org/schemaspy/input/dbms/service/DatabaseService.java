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
package org.schemaspy.input.dbms.service;

import org.schemaspy.Config;
import org.schemaspy.input.dbms.service.helper.BasicTableMeta;
import org.schemaspy.input.dbms.service.helper.RemoteTableIdentifier;
import org.schemaspy.input.dbms.xml.SchemaMeta;
import org.schemaspy.input.dbms.xml.TableMeta;
import org.schemaspy.model.*;
import org.schemaspy.validator.NameValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

import static org.schemaspy.input.dbms.service.ColumnLabel.COLUMN_NAME;
import static org.schemaspy.input.dbms.service.ColumnLabel.TABLE_NAME;
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

    private static final long THIRTY_MINUTES = 1000L*60L*30L;

    private final Clock clock;

    private final SqlService sqlService;

    private final TableService tableService;
    private final ViewService viewService;
    private final RoutineService routineService;
    private final SequenceService sequenceService;


    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public DatabaseService(Clock clock, SqlService sqlService, TableService tableService, ViewService viewService, RoutineService routineService, SequenceService sequenceService) {
        this.clock = Objects.requireNonNull(clock);
        this.sqlService = Objects.requireNonNull(sqlService);
        this.tableService = Objects.requireNonNull(tableService);
        this.viewService = Objects.requireNonNull(viewService);
        this.routineService = Objects.requireNonNull(routineService);
        this.sequenceService = Objects.requireNonNull(sequenceService);
    }

    public void gatherSchemaDetails(Config config, Database db, SchemaMeta schemaMeta, ProgressListener listener) throws SQLException {
        LOGGER.info("Gathering schema details");

        listener.startedGatheringDetails();

        DatabaseMetaData meta = sqlService.getDatabaseMetaData();

        initTables(config, db, listener, meta);
        if (config.isViewsEnabled())
            initViews(config, db, listener, meta);
        
        initCatalogs(db);
        initSchemas(db);

        initCheckConstraints(config, db);
        tableService.gatherTableIds(config, db);
        initIndexIds(config, db);
        tableService.gatherTableComments(config, db);
        tableService.gatherTableColumnComments(config, db);
        viewService.gatherViewComments(config, db);
        viewService.gatherViewColumnComments(config, db);
        initColumnTypes(config, db);
        routineService.gatherRoutines(db);
        sequenceService.gatherSequences(db);

        listener.startedConnectingTables();

        connectTables(db, listener);
        updateFromXmlMetadata(db, schemaMeta);
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
                    LOGGER.error("Failed to retrieve comment for catalog '{}' using SQL '{}'", db.getCatalog().getName(), sql, sqlException);
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
                  LOGGER.error("Failed to retrieve comment for schema '{}' using SQL '{}'", db.getSchema().getName(), sql, sqlException);
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
                viewService.gatherViewsDetails(db, view);
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
    private static String[] getTypes(Config config, String propName, String defaultValue) {
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
    private void updateFromXmlMetadata(Database db, SchemaMeta schemaMeta) throws SQLException {
        if (Objects.isNull(schemaMeta)) {
            return;
        }
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
                table = tableService.addLogicalRemoteTable(db, RemoteTableIdentifier.from(tableMeta), db.getSchema().getName());
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

    private void connectTables(Database db, ProgressListener listener) throws SQLException {
        Instant startTables = clock.instant();
        Duration durationOneTable = null;
        for (Table table : db.getTables()) {
            listener.connectingTablesProgressed(table);

            tableService.connectForeignKeys(db, table, db.getLocals());
            if (Objects.isNull(durationOneTable)) {
                durationOneTable = Duration.between(startTables, clock.instant());
                long timeLeft = durationOneTable.toMillis()*(db.getTables().size()-1);
                if (timeLeft > THIRTY_MINUTES && Config.getInstance().isExportedKeysEnabled()) {
                    String remaining = DurationFormatter.formatMS(timeLeft);
                    LOGGER.info("Estimated time remaining for connecting tables is {}, most time might be spent in getExportedKeys, you can disable getExportedKeys with `-noexportedkeys`. The implication of this is that you won't get cross schema relationships where table in analysis is FK, and the remote schema isn't analyzed", remaining);
                }
            }
        }
        Instant startViews = clock.instant();
        Duration durationOneView = null;
        for (Table view : db.getViews()) {
            listener.connectingTablesProgressed(view);

            tableService.connectForeignKeys(db, view, db.getLocals());
            if (Objects.isNull(durationOneView)) {
                durationOneView = Duration.between(startViews, clock.instant());
                long timeLeft = durationOneView.toMillis()*(db.getViews().size()-1);
                if (timeLeft > THIRTY_MINUTES && Config.getInstance().isExportedKeysEnabled()) {
                    String remaining = DurationFormatter.formatMS(timeLeft);
                    LOGGER.info("Estimated time remaining for connecting views is {}, most time might be spent in getExportedKeys, you can disable getExportedKeys with `-noexportedkeys`. The implication of this is that you won't get cross schema relationships where table in analysis is FK, and the remote schema isn't analyzed", remaining);
                }
            }
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

        void join() {
            /**
             * Wait for all of the tables to be created.
             * By default this does nothing since this implementation isn't threaded.
             */
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
                            threads.notifyAll();
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
                        Thread.currentThread().interrupt();
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
                    Thread.currentThread().interrupt();
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
        List<BasicTableMeta> basics = new ArrayList<>();
        if (!getBasicTableMetaFromSql(basics, config, db, forTables)) {
            getBasicTableMetaFromDatabaseMetaData(basics, metadata, db, forTables, types);
        }
        return basics;
    }
    private boolean getBasicTableMetaFromSql(List<BasicTableMeta> basics, Config config, Database database, boolean forTables) {
        String queryName = forTables ? "selectTablesSql" : "selectViewsSql";
        String sql = config.getDbProperties().getProperty(queryName);

        if (sql != null) {
            String clazz = forTables ? "table" : "view";
            try (PreparedStatement stmt = sqlService.prepareStatement(sql, database, null);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    basics.add(basicTableMetaFromResultSetRow(rs, forTables, clazz, database.getSchema().getName()));
                }
                return true;
            } catch (SQLException sqlException) {
                LOGGER.warn("Failed to retrieve '{}' names with custom SQL '{}", clazz, sql, sqlException);
            }
        }
        basics.clear();
        return false;
    }

    private static BasicTableMeta basicTableMetaFromResultSetRow(ResultSet rs, boolean forTables, String clazz, String schemaName) throws SQLException {
        String name = rs.getString(clazz + "_name");
        String cat = getOptionalString(rs, clazz + "_catalog");
        String sch = getOptionalString(rs, clazz + "_schema");
        if (cat == null && sch == null)
            sch = schemaName;
        String remarks = getOptionalString(rs, clazz + "_comment");
        String viewDefinition = forTables ? null : getOptionalString(rs, "view_definition");
        String rows = forTables ? getOptionalString(rs, "table_rows") : null;
        long numRows = rows == null ? -1 : Long.parseLong(rows);
        return new BasicTableMeta(cat, sch, name, clazz, remarks, viewDefinition, numRows);
    }

    private static void getBasicTableMetaFromDatabaseMetaData(List<BasicTableMeta> basics, DatabaseMetaData databaseMetaData, Database database, boolean forTables, String... types) throws SQLException {
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
    }

    /**
     * Some databases don't play nice with their metadata.
     * E.g. Oracle doesn't have a REMARKS column at all.
     * This method ignores those types of failures, replacing them with null.
     */
    private static String getOptionalString(ResultSet rs, String columnName) {
        try {
            return rs.getString(columnName);
        } catch (SQLException ignore) { //NOSONAR
            return null;
        }
    }

    private void initCheckConstraints(Config config, Database db) {
        String sql = config.getDbProperties().getProperty("selectCheckConstraintsSql");
        boolean append = Boolean.parseBoolean(config.getDbProperties().getProperty("multirowdata", "false"));
        if (sql != null) {
            try (PreparedStatement stmt = sqlService.prepareStatement(sql, db,null);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String tableName = rs.getString(TABLE_NAME);
                    Table table = db.getLocals().get(tableName);
                    if (table != null) {
                        if (append) {
                            table.getCheckConstraints().merge(rs.getString("constraint_name"), rs.getString("text"), (oldValue, newValue) -> oldValue + newValue);
                        } else {
                            table.getCheckConstraints().put(rs.getString("constraint_name"), rs.getString("text"));
                        }
                    }
                }
            } catch (SQLException sqlException) {
                // don't die just because this failed
                LOGGER.warn("Failed to retrieve check constraints using SQL '{}'", sql, sqlException);
            }
        }
    }

    private void initColumnTypes(Config config, Database db) {
        String sql = config.getDbProperties().getProperty("selectColumnTypesSql");
        if (sql != null) {

            try (PreparedStatement stmt = sqlService.prepareStatement(sql, db, null);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String tableName = rs.getString(TABLE_NAME);
                    Table table = db.getLocals().get(tableName);
                    if (table != null) {
                        String columnName = rs.getString(COLUMN_NAME);
                        TableColumn column = table.getColumn(columnName);
                        if (column != null) {
                            column.setTypeName(rs.getString("column_type"));
                            column.setShortType(getOptionalString(rs, "short_column_type"));
                        }
                    }
                }
            } catch (SQLException sqlException) {
                // don't die just because this failed
                LOGGER.warn("Failed to retrieve column type details using SQL '{}'", sql, sqlException);
            }
        }
    }

    private void initIndexIds(Config config, Database db) throws SQLException {
        String sql = config.getDbProperties().getProperty("selectIndexIdsSql");
        if (sql != null) {

            try (PreparedStatement stmt = sqlService.prepareStatement(sql, db, null);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String tableName = rs.getString(TABLE_NAME);
                    Table table = db.getLocals().get(tableName);
                    if (table != null) {
                        TableIndex index = table.getIndex(rs.getString("index_name"));
                        if (index != null)
                            index.setId(rs.getObject("index_id"));
                    }
                }
            } catch (SQLException sqlException) {
                LOGGER.warn("Failed to fetch index ids using SQL '{}'", sql, sqlException);
            }
        }
    }
}
