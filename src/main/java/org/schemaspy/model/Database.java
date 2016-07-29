/*
 * This file is a part of the SchemaSpy project (http://schemaspy.sourceforge.net).
 * Copyright (C) 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2014 John Currier
 *
 * SchemaSpy is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.schemaspy.model;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.schemaspy.Config;
import org.schemaspy.model.xml.SchemaMeta;
import org.schemaspy.model.xml.TableMeta;
import org.schemaspy.util.CaseInsensitiveMap;

public class Database {
    private final Config config;
    private final String databaseName;
    private final String catalog;
    private final String schema;
    private final Map<String, Table> tables = new CaseInsensitiveMap<Table>();
    private final Map<String, View> views = new CaseInsensitiveMap<View>();
    private final Map<String, Table> remoteTables = new CaseInsensitiveMap<Table>(); // key: schema.tableName
    private final Map<String, Table> locals = new CombinedMap(tables, views);
    private final Map<String, Routine> routines = new CaseInsensitiveMap<Routine>();
    private final DatabaseMetaData meta;
    private final Connection connection;
    private final String connectTime = new SimpleDateFormat("EEE MMM dd HH:mm z yyyy").format(new Date());
    private Set<String> sqlKeywords;
    private Pattern invalidIdentifierPattern;
    private final Logger logger = Logger.getLogger(getClass().getName());
    private final boolean fineEnabled = logger.isLoggable(Level.FINE);
	private final ProgressListener listener;

    public Database(Config config, Connection connection, DatabaseMetaData meta, String name, String catalog, String schema, SchemaMeta schemaMeta,
    				ProgressListener progressListener) throws SQLException, MissingResourceException {
        this.config = config;
        this.connection = connection;
        this.meta = meta;
        this.databaseName = name;
        this.catalog = catalog;
        this.schema = schema;
        this.listener = progressListener;

        logger.info("Gathering schema details");

        progressListener.startedGatheringDetails();

        initTables(meta);
        if (config.isViewsEnabled())
            initViews(meta);

        initCheckConstraints();
        initTableIds();
        initIndexIds();
        initTableComments();
        initTableColumnComments();
        initViewComments();
        initViewColumnComments();
        initColumnTypes();
        initRoutines();

        progressListener.startedConnectingTables();

        connectTables();
        updateFromXmlMetadata(schemaMeta);
    }

    public String getName() {
        return databaseName;
    }

    public String getCatalog() {
        return catalog;
    }

    public String getSchema() {
        return schema;
    }

    /**
     * Details of the database type that's running under the covers.
     *
     * @return null if a description wasn't specified.
     */
    public String getDescription() {
        return config.getDescription();
    }

    public Collection<Table> getTables() {
        return tables.values();
    }

    /**
     * Return a {@link Map} of all {@link Table}s keyed by their name.
     *
     * @return
     */
    public Map<String, Table> getTablesByName() {
        return tables;
    }

    public Collection<View> getViews() {
        return views.values();
    }

    public Collection<Table> getRemoteTables() {
        return remoteTables.values();
    }

    public Collection<Routine> getRoutines() {
        return routines.values();
    }

    public Connection getConnection() {
        return connection;
    }

    public DatabaseMetaData getMetaData() {
        return meta;
    }

    public String getConnectTime() {
        return connectTime;
    }

    public String getDatabaseProduct() {
        try {
            return meta.getDatabaseProductName() + " - " + meta.getDatabaseProductVersion();
        } catch (SQLException exc) {
            return "";
        }
    }

    /**
     *  "macro" to validate that a table is somewhat valid
     */
    class NameValidator {
        private final String clazz;
        private final Pattern include;
        private final Pattern exclude;
        private final Set<String> validTypes;

        /**
         * @param clazz table or view
         * @param include
         * @param exclude
         * @param verbose
         * @param validTypes
         */
        NameValidator(String clazz, Pattern include, Pattern exclude, String[] validTypes) {
            this.clazz = clazz;
            this.include = include;
            this.exclude = exclude;
            this.validTypes = new HashSet<String>();
            for (String type : validTypes)
            {
                this.validTypes.add(type.toUpperCase());
            }
        }

        /**
         * Returns <code>true</code> if the table/view name is deemed "valid"
         *
         * @param name name of the table or view
         * @param type type as returned by metadata.getTables():TABLE_TYPE
         * @return
         */
        boolean isValid(String name, String type) {
            // some databases (MySQL) return more than we wanted
            if (!validTypes.contains(type.toUpperCase()))
                return false;

            // Oracle 10g introduced problematic flashback tables
            // with bizarre illegal names
            if (name.indexOf("$") != -1) {
                if (fineEnabled) {
                    logger.fine("Excluding " + clazz + " " + name +
                                ": embedded $ implies illegal name");
                }
                return false;
            }

            if (exclude.matcher(name).matches()) {
                if (fineEnabled) {
                    logger.fine("Excluding " + clazz + " " + name +
                                ": matches exclusion pattern \"" + exclude + '"');
                }
                return false;
            }

            boolean valid = include.matcher(name).matches();
            if (fineEnabled) {
                if (valid) {
                    logger.fine("Including " + clazz + " " + name +
                                ": matches inclusion pattern \"" + include + '"');
                } else {
                    logger.fine("Excluding " + clazz + " " + name +
                                ": doesn't match inclusion pattern \"" + include + '"');
                }
            }
            return valid;
        }
    }

    /**
     * Create/initialize any tables in the schema.

     * @param metadata
     * @throws SQLException
     */
    private void initTables(final DatabaseMetaData metadata) throws SQLException {
        final Pattern include = config.getTableInclusions();
        final Pattern exclude = config.getTableExclusions();
        final int maxThreads = config.getMaxDbThreads();

        String[] types = getTypes("tableTypes", "TABLE");
        NameValidator validator = new NameValidator("table", include, exclude, types);
        List<BasicTableMeta> entries = getBasicTableMeta(metadata, true, types);

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

                if (validator.isValid(entry.name, entry.type)) {
                    new TableCreator().create(entry);
                    break;
                }
            }
        }

        // kick off the secondary threads to do the creation in parallel
        for (BasicTableMeta entry : entries) {
            if (validator.isValid(entry.name, entry.type)) {
                creator.create(entry);
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
    private void initViews(DatabaseMetaData metadata) throws SQLException {
        Pattern includeTables = config.getTableInclusions();
        Pattern excludeTables = config.getTableExclusions();

        String[] types = getTypes("viewTypes", "VIEW");
        NameValidator validator = new NameValidator("view", includeTables, excludeTables, types);

        for (BasicTableMeta entry : getBasicTableMeta(metadata, false, types)) {
            if (validator.isValid(entry.name, entry.type)) {
                View view = new View(this, entry.catalog, entry.schema, entry.name,
                                    entry.remarks, entry.viewSql);
                views.put(view.getName(), view);
                listener.gatheringDetailsProgressed(view);

                if (fineEnabled) {
                    logger.fine("Found details of view " + view.getName());
                }
            }
        }
    }

    /**
     * Collection of fundamental table/view metadata
     */
    private class BasicTableMeta
    {
        @SuppressWarnings("hiding")
        final String catalog;
        @SuppressWarnings("hiding")
        final String schema;
        final String name;
        final String type;
        final String remarks;
        final String viewSql;
        final int numRows;  // -1 if not determined

        /**
         * @param schema
         * @param name
         * @param type typically "TABLE" or "VIEW"
         * @param remarks
         * @param text optional textual SQL used to create the view
         * @param numRows number of rows, or -1 if not determined
         */
        BasicTableMeta(String catalog, String schema, String name, String type, String remarks, String text, int numRows)
        {
            this.catalog = catalog;
            this.schema = schema;
            this.name = name;
            this.type = type;
            this.remarks = remarks;
            viewSql = text;
            this.numRows = numRows;
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
    private List<BasicTableMeta> getBasicTableMeta(DatabaseMetaData metadata,
                                                    boolean forTables,
                                                    String... types) throws SQLException {
        String queryName = forTables ? "selectTablesSql" : "selectViewsSql";
        String sql = Config.getInstance().getDbProperties().getProperty(queryName);
        List<BasicTableMeta> basics = new ArrayList<BasicTableMeta>();
        ResultSet rs = null;

        if (sql != null) {
            String clazz = forTables ? "table" : "view";
            PreparedStatement stmt = null;

            try {
                stmt = prepareStatement(sql, null);
                rs = stmt.executeQuery();

                while (rs.next()) {
                    String name = rs.getString(clazz + "_name");
                    String cat = getOptionalString(rs, clazz + "_catalog");
                    String sch = getOptionalString(rs, clazz + "_schema");
                    if (cat == null && sch == null)
                        sch = schema;
                    String remarks = getOptionalString(rs, clazz + "_comment");
                    String text = forTables ? null : getOptionalString(rs, "view_definition");
                    String rows = forTables ? getOptionalString(rs, "table_rows") : null;
                    int numRows = rows == null ? -1 : Integer.parseInt(rows);

                    basics.add(new BasicTableMeta(cat, sch, name, clazz, remarks, text, numRows));
                }
            } catch (SQLException sqlException) {
                // don't die just because this failed
            	String msg = listener.recoverableExceptionEncountered("Failed to retrieve " + clazz + " names with custom SQL", sqlException, sql);
            	if (msg != null) {
            		logger.warning(msg);
            	}
            } finally {
                if (rs != null)
                    rs.close();
                if (stmt != null)
                    stmt.close();
            }
        }

        if (basics.isEmpty()) {
            rs = metadata.getTables(null, schema, "%", types);

            try {
                while (rs.next()) {
                    String name = rs.getString("TABLE_NAME");
                    String type = rs.getString("TABLE_TYPE");
                    String cat = rs.getString("TABLE_CAT");
                    String schem = rs.getString("TABLE_SCHEM");
                    String remarks = getOptionalString(rs, "REMARKS");

                    basics.add(new BasicTableMeta(cat, schem, name, type, remarks, null, -1));
                }
            } catch (SQLException exc) {
                if (forTables)
                    throw exc;

                System.out.flush();
                System.err.println();
                System.err.println("Ignoring view " + rs.getString("TABLE_NAME") + " due to exception:");
                exc.printStackTrace();
                System.err.println("Continuing analysis.");
            } finally {
                if (rs != null)
                    rs.close();
            }
        }

        return basics;
    }

    /**
     * Return a database-specific array of types from the .properties file
     * with the specified property name.
     *
     * @param propName
     * @param defaultValue
     * @return
     */
    private String[] getTypes(String propName, String defaultValue) {
        String value = config.getDbProperties().getProperty(propName, defaultValue);
        List<String> types = new ArrayList<String>();
        for (String type : value.split(",")) {
            type = type.trim();
            if (type.length() > 0)
                types.add(type);
        }

        return types.toArray(new String[types.size()]);
    }

    /**
     * Some databases don't play nice with their metadata.
     * E.g. Oracle doesn't have a REMARKS column at all.
     * This method ignores those types of failures, replacing them with null.
     */
    public String getOptionalString(ResultSet rs, String columnName)
    {
        try {
            return rs.getString(columnName);
        } catch (SQLException ignore) {
            return null;
        }
    }

    private void initCheckConstraints() throws SQLException {
        String sql = config.getDbProperties().getProperty("selectCheckConstraintsSql");
        if (sql != null) {
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                stmt = prepareStatement(sql, null);
                rs = stmt.executeQuery();

                while (rs.next()) {
                    String tableName = rs.getString("table_name");
                    Table table = locals.get(tableName);
                    if (table != null)
                        table.addCheckConstraint(rs.getString("constraint_name"), rs.getString("text"));
                }
            } catch (SQLException sqlException) {
                // don't die just because this failed
                String msg = listener.recoverableExceptionEncountered("Failed to retrieve check constraints", sqlException, sql);
            	if (msg != null) {
            		logger.warning(msg);
            	}
            } finally {
                if (rs != null)
                    rs.close();
                if (stmt != null)
                    stmt.close();
            }
        }
    }

    private void initColumnTypes() throws SQLException {
        String sql = config.getDbProperties().getProperty("selectColumnTypesSql");
        if (sql != null) {
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                stmt = prepareStatement(sql, null);
                rs = stmt.executeQuery();

                while (rs.next()) {
                    String tableName = rs.getString("table_name");
                    Table table = locals.get(tableName);
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
            		logger.warning(msg);
            	}
            } finally {
                if (rs != null)
                    rs.close();
                if (stmt != null)
                    stmt.close();
            }
        }
    }

    private void initTableIds() throws SQLException {
        String sql = config.getDbProperties().getProperty("selectTableIdsSql");
        if (sql != null) {
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                stmt = prepareStatement(sql, null);
                rs = stmt.executeQuery();

                while (rs.next()) {
                    String tableName = rs.getString("table_name");
                    Table table = locals.get(tableName);
                    if (table != null)
                        table.setId(rs.getObject("table_id"));
                }
            } catch (SQLException sqlException) {
                System.err.println();
                System.err.println(sql);
                throw sqlException;
            } finally {
                if (rs != null)
                    rs.close();
                if (stmt != null)
                    stmt.close();
            }
        }
    }

    private void initIndexIds() throws SQLException {
        String sql = config.getDbProperties().getProperty("selectIndexIdsSql");
        if (sql != null) {
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                stmt = prepareStatement(sql, null);
                rs = stmt.executeQuery();

                while (rs.next()) {
                    String tableName = rs.getString("table_name");
                    Table table = locals.get(tableName);
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
            } finally {
                if (rs != null)
                    rs.close();
                if (stmt != null)
                    stmt.close();
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
    private void initTableComments() throws SQLException {
        String sql = config.getDbProperties().getProperty("selectTableCommentsSql");
        if (sql != null) {
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                stmt = prepareStatement(sql, null);
                rs = stmt.executeQuery();

                while (rs.next()) {
                    String tableName = rs.getString("table_name");
                    Table table = locals.get(tableName);
                    if (table != null)
                        table.setComments(rs.getString("comments"));
                }
            } catch (SQLException sqlException) {
                // don't die just because this failed
                String msg = listener.recoverableExceptionEncountered("Failed to retrieve table/view comments", sqlException, sql);
            	if (msg != null) {
            		logger.warning(msg);
            	}
            } finally {
                if (rs != null)
                    rs.close();
                if (stmt != null)
                    stmt.close();
            }
        }
    }

    /**
     * Initializes view comments.
     *
     * @throws SQLException
     */
    private void initViewComments() throws SQLException {
        String sql = config.getDbProperties().getProperty("selectViewCommentsSql");
        if (sql != null) {
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                stmt = prepareStatement(sql, null);
                rs = stmt.executeQuery();

                while (rs.next()) {
                    String viewName = rs.getString("view_name");
                    if (viewName == null)
                        viewName = rs.getString("table_name");
                    Table view = views.get(viewName);

                    if (view != null)
                        view.setComments(rs.getString("comments"));
                }
            } catch (SQLException sqlException) {
                // don't die just because this failed
            	String msg = listener.recoverableExceptionEncountered("Failed to retrieve table/view comments", sqlException, sql);
            	if (msg != null) {
            		logger.warning(msg);
            	}
            } finally {
                if (rs != null)
                    rs.close();
                if (stmt != null)
                    stmt.close();
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
    private void initTableColumnComments() throws SQLException {
        String sql = config.getDbProperties().getProperty("selectColumnCommentsSql");
        if (sql != null) {
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                stmt = prepareStatement(sql, null);
                rs = stmt.executeQuery();

                while (rs.next()) {
                    String tableName = rs.getString("table_name");
                    Table table = locals.get(tableName);
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
            		logger.warning(msg);
            	}
            } finally {
                if (rs != null)
                    rs.close();
                if (stmt != null)
                    stmt.close();
            }
        }
    }

    /**
     * Initializes view column comments.
     *
     * @throws SQLException
     */
    private void initViewColumnComments() throws SQLException {
        String sql = config.getDbProperties().getProperty("selectViewColumnCommentsSql");
        if (sql != null) {
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                stmt = prepareStatement(sql, null);
                rs = stmt.executeQuery();

                while (rs.next()) {
                    String viewName = rs.getString("view_name");
                    if (viewName == null)
                        viewName = rs.getString("table_name");
                    Table view = views.get(viewName);

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
            		logger.warning(msg);
            	}
            } finally {
                if (rs != null)
                    rs.close();
                if (stmt != null)
                    stmt.close();
            }
        }
    }

    /**
     * Initializes stored procedures / functions.
     *
     * @throws SQLException
     */
    private void initRoutines() throws SQLException {
        String sql = config.getDbProperties().getProperty("selectRoutinesSql");

        if (sql != null) {
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                stmt = prepareStatement(sql, null);
                rs = stmt.executeQuery();

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
                    routines.put(routineName, routine);
                }
            } catch (SQLException sqlException) {
                // don't die just because this failed
                String msg = listener.recoverableExceptionEncountered("Failed to retrieve stored procedure/function details", sqlException, sql);
            	if (msg != null) {
            		logger.warning(msg);
            	}
            } finally {
                if (rs != null)
                    rs.close();
                if (stmt != null)
                    stmt.close();
                rs = null;
                stmt = null;
            }
        }

        sql = config.getDbProperties().getProperty("selectRoutineParametersSql");

        if (sql != null) {
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                stmt = prepareStatement(sql, null);
                rs = stmt.executeQuery();

                while (rs.next()) {
                    String routineName = rs.getString("specific_name");

                    Routine routine = routines.get(routineName);
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
            		logger.warning(msg);
            	}
            } finally {
                if (rs != null)
                    rs.close();
                if (stmt != null)
                    stmt.close();
            }
        }
    }

    /**
     * Create a <code>PreparedStatement</code> from the specified SQL.
     * The SQL can contain these named parameters (but <b>not</b> question marks).
     * <ol>
     * <li>:schema - replaced with the name of the schema
     * <li>:owner - alias for :schema
     * <li>:table - replaced with the name of the table
     * </ol>
     * @param sql String - SQL without question marks
     * @param tableName String - <code>null</code> if the statement doesn't deal with <code>Table</code>-level details.
     * @throws SQLException
     * @return PreparedStatement
     */
    public PreparedStatement prepareStatement(String sql, String tableName) throws SQLException {
        StringBuilder sqlBuf = new StringBuilder(sql);
        List<String> sqlParams = getSqlParams(sqlBuf, tableName); // modifies sqlBuf
        if (fineEnabled)
            logger.fine(sqlBuf + " " + sqlParams);
        PreparedStatement stmt = getConnection().prepareStatement(sqlBuf.toString());

        try {
            for (int i = 0; i < sqlParams.size(); ++i) {
                stmt.setString(i + 1, sqlParams.get(i).toString());
            }
        } catch (SQLException exc) {
            stmt.close();
            throw exc;
        }

        return stmt;
    }

    public Table addRemoteTable(String remoteCatalog, String remoteSchema, String remoteTableName, String baseContainer, boolean logical) throws SQLException {
        String fullName = getRemoteTableKey(remoteCatalog, remoteSchema, remoteTableName);
        Table remoteTable = remoteTables.get(fullName);
        if (remoteTable == null) {
            if (fineEnabled)
                logger.fine("Creating remote table " + fullName);

            if (logical)
                remoteTable = new LogicalRemoteTable(this, remoteCatalog, remoteSchema, remoteTableName, baseContainer);
            else
                remoteTable = new RemoteTable(this, remoteCatalog, remoteSchema, remoteTableName, baseContainer);

            if (fineEnabled)
                logger.fine("Adding remote table " + fullName);

            remoteTables.put(fullName, remoteTable);
            remoteTable.connectForeignKeys(locals);
        }

        return remoteTable;
    }

    /**
     * Return an uppercased <code>Set</code> of all SQL keywords used by a database
     *
     * @return
     * @throws SQLException
     */
    public Set<String> getSqlKeywords() throws SQLException {
        if (sqlKeywords == null) {
            // from http://www.contrib.andrew.cmu.edu/~shadow/sql/sql1992.txt:
            String[] sql92Keywords =
                ("ADA" +
                "| C | CATALOG_NAME | CHARACTER_SET_CATALOG | CHARACTER_SET_NAME" +
                "| CHARACTER_SET_SCHEMA | CLASS_ORIGIN | COBOL | COLLATION_CATALOG" +
                "| COLLATION_NAME | COLLATION_SCHEMA | COLUMN_NAME | COMMAND_FUNCTION | COMMITTED" +
                "| CONDITION_NUMBER | CONNECTION_NAME | CONSTRAINT_CATALOG | CONSTRAINT_NAME" +
                "| CONSTRAINT_SCHEMA | CURSOR_NAME" +
                "| DATA | DATETIME_INTERVAL_CODE | DATETIME_INTERVAL_PRECISION | DYNAMIC_FUNCTION" +
                "| FORTRAN" +
                "| LENGTH" +
                "| MESSAGE_LENGTH | MESSAGE_OCTET_LENGTH | MESSAGE_TEXT | MORE | MUMPS" +
                "| NAME | NULLABLE | NUMBER" +
                "| PASCAL | PLI" +
                "| REPEATABLE | RETURNED_LENGTH | RETURNED_OCTET_LENGTH | RETURNED_SQLSTATE" +
                "| ROW_COUNT" +
                "| SCALE | SCHEMA_NAME | SERIALIZABLE | SERVER_NAME | SUBCLASS_ORIGIN" +
                "| TABLE_NAME | TYPE" +
                "| UNCOMMITTED | UNNAMED" +
                "| ABSOLUTE | ACTION | ADD | ALL | ALLOCATE | ALTER | AND" +
                "| ANY | ARE | AS | ASC" +
                "| ASSERTION | AT | AUTHORIZATION | AVG" +
                "| BEGIN | BETWEEN | BIT | BIT_LENGTH | BOTH | BY" +
                "| CASCADE | CASCADED | CASE | CAST | CATALOG | CHAR | CHARACTER | CHAR_LENGTH" +
                "| CHARACTER_LENGTH | CHECK | CLOSE | COALESCE | COLLATE | COLLATION" +
                "| COLUMN | COMMIT | CONNECT | CONNECTION | CONSTRAINT" +
                "| CONSTRAINTS | CONTINUE" +
                "| CONVERT | CORRESPONDING | COUNT | CREATE | CROSS | CURRENT" +
                "| CURRENT_DATE | CURRENT_TIME | CURRENT_TIMESTAMP | CURRENT_USER | CURSOR" +
                "| DATE | DAY | DEALLOCATE | DEC | DECIMAL | DECLARE | DEFAULT | DEFERRABLE" +
                "| DEFERRED | DELETE | DESC | DESCRIBE | DESCRIPTOR | DIAGNOSTICS" +
                "| DISCONNECT | DISTINCT | DOMAIN | DOUBLE | DROP" +
                "| ELSE | END | END-EXEC | ESCAPE | EXCEPT | EXCEPTION" +
                "| EXEC | EXECUTE | EXISTS" +
                "| EXTERNAL | EXTRACT" +
                "| FALSE | FETCH | FIRST | FLOAT | FOR | FOREIGN | FOUND | FROM | FULL" +
                "| GET | GLOBAL | GO | GOTO | GRANT | GROUP" +
                "| HAVING | HOUR" +
                "| IDENTITY | IMMEDIATE | IN | INDICATOR | INITIALLY | INNER | INPUT" +
                "| INSENSITIVE | INSERT | INT | INTEGER | INTERSECT | INTERVAL | INTO | IS" +
                "| ISOLATION" +
                "| JOIN" +
                "| KEY" +
                "| LANGUAGE | LAST | LEADING | LEFT | LEVEL | LIKE | LOCAL | LOWER" +
                "| MATCH | MAX | MIN | MINUTE | MODULE | MONTH" +
                "| NAMES | NATIONAL | NATURAL | NCHAR | NEXT | NO | NOT | NULL" +
                "| NULLIF | NUMERIC" +
                "| OCTET_LENGTH | OF | ON | ONLY | OPEN | OPTION | OR" +
                "| ORDER | OUTER" +
                "| OUTPUT | OVERLAPS" +
                "| PAD | PARTIAL | POSITION | PRECISION | PREPARE | PRESERVE | PRIMARY" +
                "| PRIOR | PRIVILEGES | PROCEDURE | PUBLIC" +
                "| READ | REAL | REFERENCES | RELATIVE | RESTRICT | REVOKE | RIGHT" +
                "| ROLLBACK | ROWS" +
                "| SCHEMA | SCROLL | SECOND | SECTION | SELECT | SESSION | SESSION_USER | SET" +
                "| SIZE | SMALLINT | SOME | SPACE | SQL | SQLCODE | SQLERROR | SQLSTATE" +
                "| SUBSTRING | SUM | SYSTEM_USER" +
                "| TABLE | TEMPORARY | THEN | TIME | TIMESTAMP | TIMEZONE_HOUR | TIMEZONE_MINUTE" +
                "| TO | TRAILING | TRANSACTION | TRANSLATE | TRANSLATION | TRIM | TRUE" +
                "| UNION | UNIQUE | UNKNOWN | UPDATE | UPPER | USAGE | USER | USING" +
                "| VALUE | VALUES | VARCHAR | VARYING | VIEW" +
                "| WHEN | WHENEVER | WHERE | WITH | WORK | WRITE" +
                "| YEAR" +
                "| ZONE").split("[| ]+");

            String[] nonSql92Keywords = getMetaData().getSQLKeywords().toUpperCase().split(",\\s*");

            sqlKeywords = new HashSet<String>() {
                private static final long serialVersionUID = 1L;

                @Override
                public boolean contains(Object key) {
                    return super.contains(((String)key).toUpperCase());
                }
            };
            sqlKeywords.addAll(Arrays.asList(sql92Keywords));
            sqlKeywords.addAll(Arrays.asList(nonSql92Keywords));
        }

        return sqlKeywords;
    }

    /**
     * Return <code>id</code> quoted if required, otherwise return <code>id</code>
     *
     * @param id
     * @return
     * @throws SQLException
     */
    public String getQuotedIdentifier(String id) throws SQLException {
        // look for any character that isn't valid (then matcher.find() returns true)
        Matcher matcher = getInvalidIdentifierPattern().matcher(id);

        boolean quotesRequired = matcher.find() || getSqlKeywords().contains(id);

        if (quotesRequired) {
            // name contains something that must be quoted
            String quote = getMetaData().getIdentifierQuoteString().trim();
            return quote + id + quote;
        }

        // no quoting necessary
        return id;
    }

    /**
     * Return a <code>Pattern</code> whose matcher will return <code>true</code>
     * when run against an identifier that contains a character that is not
     * acceptable by the database without being quoted.
     */
    private Pattern getInvalidIdentifierPattern() throws SQLException {
        if (invalidIdentifierPattern == null) {
            String validChars = "a-zA-Z0-9_";
            String reservedRegexChars = "-&^";
            String extraValidChars = getMetaData().getExtraNameCharacters();
            for (int i = 0; i < extraValidChars.length(); ++i) {
                char ch = extraValidChars.charAt(i);
                if (reservedRegexChars.indexOf(ch) >= 0)
                    validChars += "\\";
                validChars += ch;
            }

            invalidIdentifierPattern = Pattern.compile("[^" + validChars + "]");
        }

        return invalidIdentifierPattern;
    }

    /**
     * Replaces named parameters in <code>sql</code> with question marks and
     * returns appropriate matching values in the returned <code>List</code> of <code>String</code>s.
     *
     * @param sql StringBuffer input SQL with named parameters, output named params are replaced with ?'s.
     * @param tableName String
     * @return List of Strings
     *
     * @see #prepareStatement(String, String)
     */
    private List<String> getSqlParams(StringBuilder sql, String tableName) {
        Map<String, String> namedParams = new HashMap<String, String>();
        @SuppressWarnings("hiding")
        String schema = getSchema();
        if (schema == null)
            schema = getName(); // some 'schema-less' db's treat the db name like a schema (unusual case)
        namedParams.put(":schema", schema);
        namedParams.put(":owner", schema); // alias for :schema
        if (tableName != null) {
            namedParams.put(":table", tableName);
            namedParams.put(":view", tableName); // alias for :table
        }

        List<String> sqlParams = new ArrayList<String>();
        int nextColon = sql.indexOf(":");
        while (nextColon != -1) {
            String paramName = new StringTokenizer(sql.substring(nextColon), " ,\"')").nextToken();
            String paramValue = namedParams.get(paramName);
            if (paramValue == null)
                throw new InvalidConfigurationException("Unexpected named parameter '" + paramName + "' found in SQL '" + sql + "'");
            sqlParams.add(paramValue);
            sql.replace(nextColon, nextColon + paramName.length(), "?"); // replace with a ?
            nextColon = sql.indexOf(":", nextColon);
        }

        return sqlParams;
    }

    /**
     * Take the supplied XML-based metadata and update our model of the schema with it
     *
     * @param schemaMeta
     * @throws SQLException
     */
    private void updateFromXmlMetadata(SchemaMeta schemaMeta) throws SQLException {
        if (schemaMeta != null) {
            config.setDescription(schemaMeta.getComments());

            // done in three passes:
            // 1: create any new tables
            // 2: add/mod columns
            // 3: connect

            // add the newly defined tables and columns first
            for (TableMeta tableMeta : schemaMeta.getTables()) {
                Table table;

                if (tableMeta.getRemoteSchema() != null || tableMeta.getRemoteCatalog() != null) {
                    // will add it if it doesn't already exist
                    table = addRemoteTable(tableMeta.getRemoteCatalog(), tableMeta.getRemoteSchema(), tableMeta.getName(), getSchema(), true);
                } else {
                    table = locals.get(tableMeta.getName());

                    if (table == null) {
                        // new table defined only in XML metadata
                        table = new LogicalTable(this, getCatalog(), getSchema(), tableMeta.getName(), tableMeta.getComments());
                        tables.put(table.getName(), table);
                    }
                }

                table.update(tableMeta);
            }

            // then tie the tables together
            for (TableMeta tableMeta : schemaMeta.getTables()) {
                Table table;

                if (tableMeta.getRemoteCatalog() != null || tableMeta.getRemoteSchema() != null) {
                    table = remoteTables.get(getRemoteTableKey(tableMeta.getRemoteCatalog(), tableMeta.getRemoteSchema(), tableMeta.getName()));
                } else {
                    table = locals.get(tableMeta.getName());
                }

                table.connect(tableMeta, locals);
            }
        }
    }

    private void connectTables() throws SQLException {
        for (Table table : tables.values()) {
            listener.connectingTablesProgressed(table);

            table.connectForeignKeys(locals);
        }

        for (Table view : views.values()) {
            listener.connectingTablesProgressed(view);

            view.connectForeignKeys(locals);
        }
    }

    /**
     * Returns a 'key' that's used to identify a remote table
     * in the remoteTables map.
     *
     * @param cat
     * @param sch
     * @param table
     * @return
     */
    public String getRemoteTableKey(String cat, String sch, String table) {
        return Table.getFullName(getName(), cat, sch, table);
    }

    /**
     * Single-threaded implementation of a class that creates tables
     */
    private class TableCreator {
        /**
         * Create a table and put it into <code>tables</code>
         */
        void create(BasicTableMeta tableMeta) throws SQLException {
            createImpl(tableMeta);
        }

        protected void createImpl(BasicTableMeta tableMeta) throws SQLException {
            Table table = new Table(Database.this, tableMeta.catalog, tableMeta.schema, tableMeta.name, tableMeta.remarks);
            if (tableMeta.numRows != -1) {
                table.setNumRows(tableMeta.numRows);
            }

            synchronized (tables) {
                tables.put(table.getName(), table);
            }

            listener.gatheringDetailsProgressed(table);

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Retrieved details of " + table.getFullName());
            }
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
        private final Set<Thread> threads = new HashSet<Thread>();
        private final int maxThreads;

        ThreadedTableCreator(int maxThreads) {
            this.maxThreads = maxThreads;
        }

        @Override
        void create(final BasicTableMeta tableMeta) throws SQLException {
            Thread runner = new Thread() {
                @Override
                public void run() {
                    try {
                        createImpl(tableMeta);
                    } catch (SQLException exc) {
                        exc.printStackTrace(); // nobody above us in call stack...dump it here
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
     * A read-only map that treats both collections of Tables and Views as one
     * combined collection.
     * This is a bit strange, but it simplifies logic that otherwise treats
     * the two as if they were one collection.
     */
    private class CombinedMap implements Map<String, Table> {
        private final Map<String, ? extends Table> map1;
        private final Map<String, ? extends Table> map2;

        public CombinedMap(Map<String, ? extends Table> map1, Map<String, ? extends Table> map2)
        {
            this.map1 = map1;
            this.map2 = map2;
        }

        @Override
		public Table get(Object name) {
            Table table = map1.get(name);
            if (table == null)
                table = map2.get(name);
            return table;
        }

        @Override
		public int size() {
            return map1.size() + map2.size();
        }

        @Override
		public boolean isEmpty() {
            return map1.isEmpty() && map2.isEmpty();
        }

        @Override
		public boolean containsKey(Object key) {
            return map1.containsKey(key) || map2.containsKey(key);
        }

        @Override
		public boolean containsValue(Object value) {
            return map1.containsValue(value) || map2.containsValue(value);
        }

        @Override
		public Table put(String name, Table table) {
            throw new UnsupportedOperationException();
        }

        /**
         * Warning: potentially expensive operation
         */
        @Override
		public Set<String> keySet() {
            return getCombined().keySet();
        }

        /**
         * Warning: potentially expensive operation
         */
        @Override
		public Set<Map.Entry<String, Table>> entrySet() {
            return getCombined().entrySet();
        }

        /**
         * Warning: potentially expensive operation
         */
        @Override
		public Collection<Table> values() {
            return getCombined().values();
        }

        private Map<String, Table> getCombined() {
            Map<String, Table> all = new CaseInsensitiveMap<Table>(size());
            all.putAll(map1);
            all.putAll(map2);
            return all;
        }

        @Override
		public Table remove(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
		public void putAll(Map<? extends String, ? extends Table> table) {
            throw new UnsupportedOperationException();
        }

        @Override
		public void clear() {
            throw new UnsupportedOperationException();
        }
    }
}
