package org.schemaspy.service;

import org.schemaspy.Config;
import org.schemaspy.DbDriverLoader;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.model.Database;
import org.schemaspy.model.InvalidConfigurationException;
import org.schemaspy.util.ConnectionURLBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by rkasa on 2016-12-10.
 */
@Service
public class SqlService {

    private final CommandLineArguments commandLineArguments;

    private final Logger logger = Logger.getLogger(getClass().getName());
    private final boolean fineEnabled = logger.isLoggable(Level.FINE);

    private Connection connection;
    private DatabaseMetaData meta;

    private String defaultSchema;
    private String databaseName;

    public SqlService(CommandLineArguments commandLineArguments) {
        this.commandLineArguments = Objects.requireNonNull(commandLineArguments);
    }

    public Connection getConnection() {
        return connection;
    }

    public DatabaseMetaData getMeta() {
        return meta;
    }

    public DatabaseMetaData connect(Config config) throws IOException, SQLException {
        Properties properties = config.determineDbProperties(commandLineArguments.getDatabaseType());

        ConnectionURLBuilder urlBuilder = new ConnectionURLBuilder(config, properties);
        if (config.getDb() == null)
            config.setDb(urlBuilder.build());

        String driverClass = properties.getProperty("driver");
        String driverPath = properties.getProperty("driverPath");
        if (driverPath == null)
            driverPath = "";

        if (config.getDriverPath() != null)
            driverPath = config.getDriverPath();

        DbDriverLoader driverLoader = new DbDriverLoader();
        connection = driverLoader.getConnection(config, urlBuilder.build(), driverClass, driverPath);

        meta = connection.getMetaData();

        databaseName = config.getDb();
        defaultSchema = commandLineArguments.getSchema();

        if (config.isEvaluateAllEnabled()) {
            return null;    // no database to return
        }

        return meta;
    }

    /**
     * Create a <code>PreparedStatement</code> from the specified SQL.
     * The SQL can contain these named parameters (but <b>not</b> question marks).
     * <ol>
     * <li>:schema - replaced with the name of the schema
     * <li>:owner - alias for :schema
     * <li>:table - replaced with the name of the table
     * </ol>
     *
     * @param sql       String - SQL without question marks
     * @param tableName String - <code>null</code> if the statement doesn't deal with <code>Table</code>-level details.
     * @return PreparedStatement
     * @throws SQLException
     */
    public PreparedStatement prepareStatement(String sql, Database db, String tableName) throws SQLException {
        StringBuilder sqlBuf = new StringBuilder(sql);
        List<String> sqlParams = getSqlParams(sqlBuf, db.getName(), db.getCatalog().getName(), db.getSchema().getName(), tableName); // modifies sqlBuf
        if (fineEnabled)
            logger.fine(sqlBuf + " " + sqlParams);

        PreparedStatement stmt = connection.prepareStatement(sqlBuf.toString());
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

    /**
     * Replaces named parameters in <code>sql</code> with question marks and
     * returns appropriate matching values in the returned <code>List</code> of <code>String</code>s.
     *
     * @param sql       StringBuffer input SQL with named parameters, output named params are replaced with ?'s.
     * @param tableName String
     * @return List of Strings
     * @see #prepareStatement(String, String)
     */
    private List<String> getSqlParams(StringBuilder sql, String dbName, String catalog, String schema, String tableName) {
        Map<String, String> namedParams = new HashMap<String, String>();
        if (schema == null) {
            schema = dbName; // some 'schema-less' db's treat the db name like a schema (unusual case)
        }
        
        namedParams.put(":dbname", dbName);
        namedParams.put(":schema", schema);
        namedParams.put(":owner", schema); // alias for :schema
        if (tableName != null) {
            namedParams.put(":table", tableName);
            namedParams.put(":view", tableName); // alias for :table
        }
        if (catalog != null) {
            namedParams.put(":catalog", catalog);
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

    public PreparedStatement prepareStatement(String sqlQuery) throws SQLException {
        return connection.prepareStatement(sqlQuery);
    }
}
