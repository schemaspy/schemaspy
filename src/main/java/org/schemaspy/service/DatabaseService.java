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
 */
public class DatabaseService {

    private final TableService tableService;

    private final ViewService viewService;

    private final SqlService sqlService;

    private final ProgressListener progressListener;

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public DatabaseService(TableService tableService, ViewService viewService, SqlService sqlService, ProgressListener progressListener) {
        this.tableService = Objects.requireNonNull(tableService);
        this.viewService = Objects.requireNonNull(viewService);
        this.sqlService = Objects.requireNonNull(sqlService);
        this.progressListener = Objects.requireNonNull(progressListener);
    }

    public void gatheringSchemaDetails(Config config, Database db) throws SQLException {
        LOGGER.info("Gathering schema details");

        progressListener.startedGatheringDetails();

        DatabaseMetaData meta = sqlService.getMeta();

        initTables(config, db, meta);
        if (config.isViewsEnabled())
            initViews(config, db, meta);
        
        initCatalogs(db);
        initSchemas(db);

        initCheckConstraints(config, db);
        initTableIds(config, db);
        initIndexIds(config, db);
        initTableComments(config, db);
        initTableColumnComments(config, db);
        initViewComments(config, db);
        initViewColumnComments(config, db);
        initColumnTypes(config, db);
        initRoutines(config, db);

        progressListener.startedConnectingTables();

        connectTables(db);
        updateFromXmlMetadata(config, db, db.getSchemaMeta());
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
                    LOGGER.error("Failed to initCatalogs with SQL: '{}'",sql, sqlException);
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
                  LOGGER.error("Failed to initSchemas with SQL: '{}'",sql, sqlException);
                  throw sqlException;
              }
          }
    }

    /**
     * Create/initialize any tables in the schema.

     * @param metadata
     * @throws SQLException
     */
    private void initTables(Config config, Database db, final DatabaseMetaData metadata) throws SQLException {
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
                    new TableCreator().create(db, entry);
                    break;
                }
            }
        }

        // kick off the secondary threads to do the creation in parallel
        for (BasicTableMeta entry : entries) {
            if (validator.isValid(entry.getName(), entry.getType())) {
                creator.create(db, entry);
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
    private void initViews(Config config, Database db, DatabaseMetaData metadata) throws SQLException {
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
                progressListener.gatheringDetailsProgressed(view);

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
                config.setDescription(schemaMeta.getComments());
            }

            // done in three passes:
            // 1: create any new tables
            // 2: add/mod columns
            // 3: connect

            // add the newly defined tables and columns first
            for (TableMeta tableMeta : schemaMeta.getTables()) {
                Table table = addOrGetTable(db, tableMeta);
                table.update(tableMeta);
            }

            // then tie the tables together
            connectSchemaMeta(db, schemaMeta.getTables());
        }
    }

    private Table addOrGetTable(Database database, TableMeta tableMeta) throws SQLException {
        Table table;
        if (tableMeta.getRemoteSchema() != null || tableMeta.getRemoteCatalog() != null) {
            // will add it if it doesn't already exist
            table = tableService.addRemoteTable(database, tableMeta.getRemoteCatalog(), tableMeta.getRemoteSchema(), tableMeta.getName(), database.getSchema().getName(), true);
        } else {
            table = database.getLocals().get(tableMeta.getName());

            if (table == null) {
                // new table defined only in XML metadata
                table = new LogicalTable(database, database.getCatalog().getName(), database.getSchema().getName(), tableMeta.getName(), tableMeta.getComments());
                database.getTablesMap().put(table.getName(), table);
            }
        }
        return table;
    }

    private void connectSchemaMeta(Database database, List<TableMeta> tableMetas) {
        for (TableMeta tableMeta : tableMetas) {
            Table table;

            if (tableMeta.getRemoteCatalog() != null || tableMeta.getRemoteSchema() != null) {
                table = database.getRemoteTablesMap().get(database.getRemoteTableKey(tableMeta.getRemoteCatalog(), tableMeta.getRemoteSchema(), tableMeta.getName()));
            } else {
                table = database.getLocals().get(tableMeta.getName());
            }

            tableService.connect(database, table, tableMeta, database.getLocals());
        }
    }

    private void connectTables(Database db) throws SQLException {
        for (Table table : db.getTables()) {
            progressListener.connectingTablesProgressed(table);

            tableService.connectForeignKeys(db, table, db.getLocals());
        }

        for (Table view : db.getViews()) {
            progressListener.connectingTablesProgressed(view);

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
        void create(Database db, BasicTableMeta tableMeta) throws SQLException {
            createImpl(db, tableMeta);
        }

        protected void createImpl(Database db, BasicTableMeta tableMeta) throws SQLException {
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

            progressListener.gatheringDetailsProgressed(table);

            LOGGER.debug("Retrieved details of {}", table.getFullName());
        }

        /**
         * Wait for all of the tables to be created.
         * By default this does nothing since this implementation isn't threaded.
         */
        void join() {
            //NO-OP in single threaded mode
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
        void create(Database db, BasicTableMeta tableMeta) {
            Thread runner = new Thread() {
                @Override
                public void run() {
                    try {
                        createImpl(db, tableMeta);
                    } catch (SQLException exc) {
                        LOGGER.error("Failed to create Table {}",tableMeta.getName(),exc);
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
        if (getBasicTableMetaFromSQL(config, db, forTables, basics)) {
            return basics;
        } else {
           return getBasicTableMetaFromDatabaseMetaData(db, metadata, forTables, types);
        }
    }

    private boolean getBasicTableMetaFromSQL(Config config, Database database, boolean forTables, List<BasicTableMeta> basics) throws SQLException {
        String queryName = forTables ? "selectTablesSql" : "selectViewsSql";
        String sql = config.getDbProperties().getProperty(queryName);
        if (Objects.isNull(sql)) {
            return false;
        }
        String clazz = forTables ? "table" : "view";
        try (PreparedStatement stmt = sqlService.prepareStatement(sql, database, null);
             ResultSet rs = stmt.executeQuery()) {

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
        } catch (SQLException sqlException) {
            LOGGER.warn("Failed to retrieve {} names using SQL: '{}'", clazz, sql, sqlException);
            return false;
        }
        return true;
    }

    private List<BasicTableMeta> getBasicTableMetaFromDatabaseMetaData(
            Database database,
            DatabaseMetaData databaseMetaData,
            boolean forTables,
            String...types) throws SQLException {
        List<BasicTableMeta> basics = new ArrayList<>();
        try (ResultSet rs = databaseMetaData.getTables(
                null,
                database.getSchema().getName(),
                "%",
                types)){
            String lastTableName = null;
            while (rs.next()) {
                try {
                    String name = rs.getString("TABLE_NAME");
                    lastTableName = name;
                    String type = rs.getString("TABLE_TYPE");
                    String cat = rs.getString("TABLE_CAT");
                    String schem = rs.getString("TABLE_SCHEM");
                    String remarks = getOptionalString(rs, "REMARKS");

                    basics.add(new BasicTableMeta(cat, schem, name, type, remarks, null, -1));
                } catch (SQLException sqlex) {
                    if (forTables) {
                        LOGGER.error("Failed to retrive table information", sqlex);
                        throw sqlex;
                    }
                    LOGGER.warn("Ignoring view '{}' due to exception", lastTableName, sqlex);
                }
            }
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

    private void initCheckConstraints(Config config, Database db) {
        String sql = config.getDbProperties().getProperty("selectCheckConstraintsSql");
        if (sql != null) {
            try (PreparedStatement stmt = sqlService.prepareStatement(sql, db,null);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String tableName = rs.getString(ColumnLabels.TABLE_NAME_LOWER);
                    Table table = db.getLocals().get(tableName);
                    if (table != null)
                        table.addCheckConstraint(rs.getString("constraint_name"), rs.getString("text"));
                }
            } catch (SQLException sqlException) {
                LOGGER.warn("Failed to retrieve check constraints using SQL: '{}'", sql, sqlException);
            }
        }
    }

    private void initColumnTypes(Config config, Database db) {
        String sql = config.getDbProperties().getProperty("selectColumnTypesSql");
        if (sql != null) {

            try (PreparedStatement stmt = sqlService.prepareStatement(sql, db, null);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String tableName = rs.getString(ColumnLabels.TABLE_NAME_LOWER);
                    Table table = db.getLocals().get(tableName);
                    if (table != null) {
                        String columnName = rs.getString(ColumnLabels.COLUMN_NAME_LOWER);
                        TableColumn column = table.getColumn(columnName);
                        if (column != null) {
                            column.setTypeName(rs.getString("column_type"));
                            column.setShortType(getOptionalString(rs, "short_column_type"));
                        }
                    }
                }
            } catch (SQLException sqlException) {
                LOGGER.warn("Failed to retrieve column type details using SQL: '{}'", sql, sqlException);
            }
        }
    }

    private void initTableIds(Config config, Database db) throws SQLException {
        String sql = config.getDbProperties().getProperty("selectTableIdsSql");
        if (sql != null) {

            try (PreparedStatement stmt = sqlService.prepareStatement(sql, db, null);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String tableName = rs.getString(ColumnLabels.TABLE_NAME_LOWER);
                    Table table = db.getLocals().get(tableName);
                    if (table != null)
                        table.setId(rs.getObject("table_id"));
                }
            } catch (SQLException sqlException) {
                LOGGER.error("Failed to initTableIds with SQL: '{}'", sql, sqlException);
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
                    String tableName = rs.getString(ColumnLabels.TABLE_NAME_LOWER);
                    Table table = db.getLocals().get(tableName);
                    if (table != null) {
                        TableIndex index = table.getIndex(rs.getString("index_name"));
                        if (index != null)
                            index.setId(rs.getObject("index_id"));
                    }
                }
            } catch (SQLException sqlException) {
                LOGGER.error("Failed to initIndexIds with SQL: '{}'", sql, sqlException);
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
    private void initTableComments(Config config, Database db) {
        String sql = config.getDbProperties().getProperty("selectTableCommentsSql");
        if (sql != null) {

            try (PreparedStatement stmt = sqlService.prepareStatement(sql, db, null);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String tableName = rs.getString(ColumnLabels.TABLE_NAME_LOWER);
                    Table table = db.getLocals().get(tableName);
                    if (table != null)
                        table.setComments(rs.getString(ColumnLabels.COMMENTS_LOWER));
                }
            } catch (SQLException sqlException) {
                LOGGER.warn("Failed to retrieve table comments using SQL: '{}'", sql, sqlException);
            }
        }
    }

    /**
     * Initializes view comments.
     *
     * @throws SQLException
     */
    private void initViewComments(Config config, Database db) {
        String sql = config.getDbProperties().getProperty("selectViewCommentsSql");
        if (sql != null) {

            try (PreparedStatement stmt = sqlService.prepareStatement(sql, db, null);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String viewName = rs.getString("view_name");
                    if (viewName == null)
                        viewName = rs.getString(ColumnLabels.TABLE_NAME_LOWER);
                    Table view = db.getViewsMap().get(viewName);

                    if (view != null)
                        view.setComments(rs.getString(ColumnLabels.COMMENTS_LOWER));
                }
            } catch (SQLException sqlException) {
                LOGGER.warn("Failed to retrieve view comments using SQL: '{}'", sql, sqlException);
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
    private void initTableColumnComments(Config config, Database db) {
        String sql = config.getDbProperties().getProperty("selectColumnCommentsSql");
        if (sql != null) {

            try (PreparedStatement stmt = sqlService.prepareStatement(sql, db, null);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String tableName = rs.getString(ColumnLabels.TABLE_NAME_LOWER);
                    Table table = db.getLocals().get(tableName);
                    if (table != null) {
                        TableColumn column = table.getColumn(rs.getString(ColumnLabels.COLUMN_NAME_LOWER));
                        if (column != null)
                            column.setComments(rs.getString(ColumnLabels.COMMENTS_LOWER));
                    }
                }
            } catch (SQLException sqlException) {
                LOGGER.warn("Failed to retrieve table column comments using SQL: '{}'", sql, sqlException);
            }
        }
    }

    /**
     * Initializes view column comments.
     *
     * @throws SQLException
     */
    private void initViewColumnComments(Config config, Database db) {
        String sql = config.getDbProperties().getProperty("selectViewColumnCommentsSql");
        if (sql != null) {

            try (PreparedStatement stmt = sqlService.prepareStatement(sql, db, null);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String viewName = rs.getString("view_name");
                    if (viewName == null)
                        viewName = rs.getString(ColumnLabels.TABLE_NAME_LOWER);
                    Table view = db.getViewsMap().get(viewName);

                    if (view != null) {
                        TableColumn column = view.getColumn(rs.getString(ColumnLabels.COLUMN_NAME_LOWER));
                        if (column != null)
                            column.setComments(rs.getString(ColumnLabels.COMMENTS_LOWER));
                    }
                }
            } catch (SQLException sqlException) {
                LOGGER.warn("Failed to retrieve view column comments using SQL: '{}'", sql, sqlException);
            }
        }
    }

    /**
     * Initializes stored procedures / functions.
     *
     * @throws SQLException
     */
    private void initRoutines(Config config, Database db) {
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
                LOGGER.warn("Failed to retrieve stored procedure/function details using SQL: '{}'", sql, sqlException);
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
                LOGGER.warn("Failed to retrieve stored procedure/function parameter details using SQL: '{}'", sql, sqlException);
            }
        }
    }

}
