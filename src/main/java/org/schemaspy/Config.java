/*
 * This file is a part of the SchemaSpy project (http://schemaspy.org).
 * Copyright (C) 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011 John Currier
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
package org.schemaspy;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.schemaspy.model.InvalidConfigurationException;
import org.schemaspy.util.DbSpecificConfig;
import org.schemaspy.util.Dot;
import org.schemaspy.util.PasswordReader;
import org.schemaspy.view.DefaultSqlFormatter;
import org.schemaspy.view.SqlFormatter;

/**
 * Configuration of a SchemaSpy run
 *
 * @author John Currier
 */
public class Config
{
    private static Config instance;
    private final List<String> options;
    private Map<String, String> dbSpecificOptions;
    private Map<String, String> originalDbSpecificOptions;
    private boolean helpRequired;
    private boolean dbHelpRequired;
    private File outputDir;
    private File graphvizDir;
    private String dbType;
    private String catalog;
    private String schema;
    private List<String> schemas;
    private String user;
    private Boolean singleSignOn;
    private Boolean noSchema;
    private String password;
    private Boolean promptForPassword;
    private String db;
    private String host;
    private Integer port;
    private String server;
    private String meta;
    private Pattern tableInclusions;
    private Pattern tableExclusions;
    private Pattern columnExclusions;
    private Pattern indirectColumnExclusions;
    private String userConnectionPropertiesFile;
    private Properties userConnectionProperties;
    private Integer maxDbThreads;
    private Integer maxDetailedTables;
    private String driverPath;
    private String css;
    private String charset;
    private String font;
    private Integer fontSize;
    private String description;
    private Properties dbProperties;
    private String dbPropertiesLoadedFrom;
    private Level logLevel;
    private SqlFormatter sqlFormatter;
    private String sqlFormatterClass;
    private Boolean generateHtml;
    private Boolean includeImpliedConstraints;
    private Boolean logoEnabled;
    private Boolean rankDirBugEnabled;
    private Boolean encodeCommentsEnabled;
    private Boolean numRowsEnabled;
    private Boolean viewsEnabled;
    private Boolean meterEnabled;
    private Boolean railsEnabled;
    private Boolean evaluteAll;
    private Boolean highQuality;
    private Boolean lowQuality;
    private String schemaSpec;  // used in conjunction with evaluateAll
    private boolean hasOrphans = false;
    private boolean hasRoutines = false;
    private boolean populating = false;
    private List<String> columnDetails;
    public static final String DOT_CHARSET = "UTF-8";
    private static final String ESCAPED_EQUALS = "\\=";
    private static final String DEFAULT_TABLE_INCLUSION = ".*"; // match everything
    private static final String DEFAULT_TABLE_EXCLUSION = "";   // match nothing
    private static final String DEFAULT_COLUMN_EXCLUSION = "[^.]";  // match nothing

    /**
     * Default constructor. Intended for when you want to inject properties
     * independently (i.e. not from a command line interface).
     */
    public Config()
    {
        if (instance == null)
            setInstance(this);
        options = new ArrayList<String>();
    }

    /**
     * Construct a configuration from an array of options (e.g. from a command
     * line interface).
     *
     * @param options
     */
    public Config(String[] argv)
    {
        setInstance(this);
        options = fixupArgs(Arrays.asList(argv));

        helpRequired =  options.remove("-?") ||
                        options.remove("/?") ||
                        options.remove("?") ||
                        options.remove("-h") ||
                        options.remove("-help") ||
                        options.remove("--help");
        dbHelpRequired =  options.remove("-dbHelp") || options.remove("-dbhelp");
    }

    public static Config getInstance() {
        if (instance == null)
            instance = new Config();

        return instance;
    }

    /**
     * Sets the global instance.
     *
     * Useful for things like selecting a specific configuration in a UI.
     *
     * @param config
     */
    public static void setInstance(Config config) {
        instance = config;
    }

    public void setHtmlGenerationEnabled(boolean generateHtml) {
        this.generateHtml = generateHtml;
    }

    public boolean isHtmlGenerationEnabled() {
        if (generateHtml == null)
            generateHtml = !options.remove("-nohtml");

        return generateHtml;
    }

    public void setImpliedConstraintsEnabled(boolean includeImpliedConstraints) {
        this.includeImpliedConstraints = includeImpliedConstraints;
    }

    public boolean isImpliedConstraintsEnabled() {
        if (includeImpliedConstraints == null)
            includeImpliedConstraints = !options.remove("-noimplied");

        return includeImpliedConstraints;
    }

    public void setOutputDir(String outputDirName) {
        if (outputDirName.endsWith("\""))
            outputDirName = outputDirName.substring(0, outputDirName.length() - 1);

        setOutputDir(new File(outputDirName));
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    public File getOutputDir() {
        if (outputDir == null) {
            setOutputDir(pullRequiredParam("-o"));
        }

        return outputDir;
    }

    /**
     * Set the path to Graphviz so we can find dot to generate ER diagrams
     *
     * @param graphvizDir
     */
    public void setGraphvizDir(String graphvizDir) {
        if (graphvizDir.endsWith("\""))
            graphvizDir = graphvizDir.substring(0, graphvizDir.length() - 1);

        setGraphvizDir(new File(graphvizDir));
    }

    /**
     * Set the path to Graphviz so we can find dot to generate ER diagrams
     *
     * @param graphvizDir
     */
    public void setGraphvizDir(File graphvizDir) {
        this.graphvizDir = graphvizDir;
    }

    /**
     * Return the path to Graphviz (used to find the dot executable to run to
     * generate ER diagrams).<p/>
     *
     * Returns {@link #getDefaultGraphvizPath()} if a specific Graphviz path
     * was not specified.
     *
     * @return
     */
    public File getGraphvizDir() {
        if (graphvizDir == null) {
            String gv = pullParam("-gv");
            if (gv != null) {
                setGraphvizDir(gv);
            } else {
                // expect to find Graphviz's bin directory on the PATH
            }
        }

        return graphvizDir;
    }

    /**
     * Meta files are XML-based files that provide additional metadata
     * about the schema being evaluated.<p>
     * <code>meta</code> is either the name of an individual XML file or
     * the directory that contains meta files.<p>
     * If a directory is specified then it is expected to contain files
     * matching the pattern <code>[schema].meta.xml</code>.
     * For databases that don't have schema substitute database for schema.
     * @param meta
     */
    public void setMeta(String meta) {
        this.meta = meta;
    }

    public String getMeta() {
        if (meta == null)
            meta = pullParam("-meta");
        return meta;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public String getDbType() {
        if (dbType == null) {
            dbType = pullParam("-t");
            if (dbType == null)
                dbType = "ora";
        }

        return dbType;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public String getDb() {
        if (db == null)
            db = pullParam("-db");
        return db;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getCatalog() {
        if (catalog == null)
            catalog = pullParam("-cat");
        return catalog;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getSchema() {
        if (schema == null)
            schema = pullParam("-s");
        return schema;
    }

    /**
     * Some databases types (e.g. older versions of Informix) don't really
     * have the concept of a schema but still return true from
     * {@link DatabaseMetaData#supportsSchemasInTableDefinitions()}.
     * This option lets you ignore that and treat all the tables
     * as if they were in one flat namespace.
     */
    public boolean isSchemaDisabled() {
        if (noSchema == null)
            noSchema = options.remove("-noschema");

        return noSchema;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        if (host == null)
            host = pullParam("-host");
        return host;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getPort() {
        if (port == null)
            try {
                port = Integer.valueOf(pullParam("-port"));
            } catch (Exception notSpecified) {}
        return port;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getServer() {
        if (server == null) {
            server = pullParam("-server");
        }

        return server;
    }

    public void setUser(String user) {
        this.user = user;
    }

    /**
     * User used to connect to the database.
     * Required unless single sign-on is enabled
     * (see {@link #setSingleSignOn(boolean)}).
     * @return
     */
    public String getUser() {
        if (user == null) {
            if (!isSingleSignOn())
                user = pullRequiredParam("-u");
            else
                user = pullParam("-u");
        }
        return user;
    }

    /**
     * By default a "user" (as specified with -u) is required.
     * This option allows disabling of that requirement for
     * single sign-on environments.
     *
     * @param enabled defaults to <code>false</code>
     */
    public void setSingleSignOn(boolean enabled) {
        singleSignOn = enabled;
    }

    /**
     * @see #setSingleSignOn(boolean)
     */
    public boolean isSingleSignOn() {
        if (singleSignOn == null)
            singleSignOn = options.remove("-sso");

        return singleSignOn;
    }

    /**
     * Set the password used to connect to the database.
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @see #setPassword(String)
     * @return
     */
    public String getPassword() {
        if (password == null)
            password = pullParam("-p");

        if (password == null && isPromptForPasswordEnabled())
            password = new String(PasswordReader.getInstance().readPassword("Password: "));

        if (password == null) {
            // if -pfp is enabled when analyzing multiple schemas then
            // we don't want to send the password on the command line,
            // so see if it was passed in the environment (not ideal, but safer)
            password = System.getenv("schemaspy.pw");
        }

        return password;
    }

    /**
     * Set to <code>true</code> to prompt for the password
     * @param promptForPassword
     */
    public void setPromptForPasswordEnabled(boolean promptForPassword) {
        this.promptForPassword = promptForPassword;
    }

    /**
     * @see #setPromptForPasswordEnabled(boolean)
     * @return
     */
    public boolean isPromptForPasswordEnabled() {
        if (promptForPassword == null) {
            promptForPassword = options.remove("-pfp");
        }

        return promptForPassword;
    }

    public void setMaxDetailedTabled(int maxDetailedTables) {
        this.maxDetailedTables = new Integer(maxDetailedTables);
    }

    public int getMaxDetailedTables() {
        if (maxDetailedTables == null) {
            int max = 300; // default
            try {
                max = Integer.parseInt(pullParam("-maxdet"));
            } catch (Exception notSpecified) {}

            maxDetailedTables = new Integer(max);
        }

        return maxDetailedTables.intValue();
    }

    public String getConnectionPropertiesFile() {
        return userConnectionPropertiesFile;
    }

    /**
     * Properties from this file (in key=value pair format) are passed to the
     * database connection.<br>
     * user (from -u) and password (from -p) will be passed in the
     * connection properties if specified.
     * @param propertiesFilename
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void setConnectionPropertiesFile(String propertiesFilename) throws FileNotFoundException, IOException {
        if (userConnectionProperties == null)
            userConnectionProperties = new Properties();
        userConnectionProperties.load(new FileInputStream(propertiesFilename));
        userConnectionPropertiesFile = propertiesFilename;
    }

    /**
     * Returns a {@link Properties} populated either from the properties file specified
     * by {@link #setConnectionPropertiesFile(String)}, the properties specified by
     * {@link #setConnectionProperties(String)} or not populated.
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public Properties getConnectionProperties() throws FileNotFoundException, IOException {
        if (userConnectionProperties == null) {
            String props = pullParam("-connprops");
            if (props != null) {
                if (props.indexOf(ESCAPED_EQUALS) != -1) {
                    setConnectionProperties(props);
                } else {
                    setConnectionPropertiesFile(props);
                }
            } else {
                userConnectionProperties = new Properties();
            }
        }

        return userConnectionProperties;
    }

    /**
     * Specifies connection properties to use in the format:
     * <code>key1\=value1;key2\=value2</code><br>
     * user (from -u) and password (from -p) will be passed in the
     * connection properties if specified.<p>
     * This is an alternative form of passing connection properties than by file
     * (see {@link #setConnectionPropertiesFile(String)})
     *
     * @param properties
     */
    public void setConnectionProperties(String properties) {
        userConnectionProperties = new Properties();

        StringTokenizer tokenizer = new StringTokenizer(properties, ";");
        while (tokenizer.hasMoreElements()) {
            String pair = tokenizer.nextToken();
            int index = pair.indexOf(ESCAPED_EQUALS);
            if (index != -1) {
                String key = pair.substring(0, index);
                String value = pair.substring(index + ESCAPED_EQUALS.length());
                userConnectionProperties.put(key, value);
            }
        }
    }

    public void setDriverPath(String driverPath) {
        this.driverPath = driverPath;
    }

    public String getDriverPath() {
        if (driverPath == null)
            driverPath = pullParam("-dp");

        // was previously -cp:
        if (driverPath == null)
            driverPath = pullParam("-cp");

        return driverPath;
    }

    /**
     * The filename of the cascading style sheet to use.
     * Note that this file is parsed and used to determine characteristics
     * of the generated diagrams, so it must contain specific settings that
     * are documented within schemaSpy.css.<p>
     *
     * Defaults to <code>"schemaSpy.css"</code>.
     *
     * @param css
     */
    public void setCss(String css) {
        this.css = css;
    }

    public String getCss() {
        if (css == null) {
            css = pullParam("-css");
            if (css == null)
                css = "layout/schemaSpy.css";
        }
        return css;
    }

    /**
     * The font to use within diagrams.  Modify the .css to specify HTML fonts.
     *
     * @param font
     */
    public void setFont(String font) {
        this.font = font;
    }

    /**
     * @see #setFont(String)
     */
    public String getFont() {
        if (font == null) {
            font = pullParam("-font");
            if (font == null)
                font = "Helvetica";
        }
        return font;
    }

    /**
     * The font size to use within diagrams.  This is the size of the font used for
     * 'large' (e.g. not 'compact') diagrams.<p>
     *
     * Modify the .css to specify HTML font sizes.<p>
     *
     * Defaults to 11.
     *
     * @param fontSize
     */
    public void setFontSize(int fontSize) {
        this.fontSize = new Integer(fontSize);
    }

    /**
     * @see #setFontSize(int)
     * @return
     */
    public int getFontSize() {
        if (fontSize == null) {
            int size = 11; // default
            try {
                size = Integer.parseInt(pullParam("-fontsize"));
            } catch (Exception notSpecified) {}

            fontSize = new Integer(size);
        }

        return fontSize.intValue();
    }

    /**
     * The character set to use within HTML pages (defaults to <code>"ISO-8859-1"</code>).
     *
     * @param charset
     */
    public void setCharset(String charset) {
        this.charset = charset;
    }

    /**
     * @see #setCharset(String)
     */
    public String getCharset() {
        if (charset == null) {
            charset = pullParam("-charset");
            if (charset == null)
                charset = "ISO-8859-1";
        }
        return charset;
    }

    /**
     * Description of schema that gets display on main pages.
     *
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see #setDescription(String)
     */
    public String getDescription() {
        if (description == null)
            description = pullParam("-desc");
        return description;
    }

    /**
     * Maximum number of threads to use when querying database metadata information.
     *
     * @param maxDbThreads
     */
    public void setMaxDbThreads(int maxDbThreads) {
        this.maxDbThreads = new Integer(maxDbThreads);
    }

    /**
     * @see #setMaxDbThreads(int)
     * @throws InvalidConfigurationException if unable to load properties
     */
    public int getMaxDbThreads() throws InvalidConfigurationException {
        if (maxDbThreads == null) {
            Properties properties;
            try {
                properties = determineDbProperties(getDbType());
            } catch (IOException exc) {
                throw new InvalidConfigurationException("Failed to load properties for " + getDbType() + ": " + exc)
                                .setParamName("-type");
            }

            final int defaultMax = 15;  // not scientifically derived
            int max = defaultMax;
            String threads = properties.getProperty("dbThreads");
            if (threads == null)
                threads = properties.getProperty("dbthreads");
            if (threads != null)
                max = Integer.parseInt(threads);
            threads = pullParam("-dbThreads");
            if (threads == null)
                threads = pullParam("-dbthreads");
            if (threads != null)
                max = Integer.parseInt(threads);
            if (max < 0)
                max = defaultMax;
            else if (max == 0)
                max = 1;

            maxDbThreads = new Integer(max);
        }

        return maxDbThreads.intValue();
    }

    public boolean isLogoEnabled() {
        if (logoEnabled == null)
            logoEnabled = !options.remove("-nologo");

        return logoEnabled;
    }

    /**
     * Don't use this unless absolutely necessary as it screws up the layout
     *
     * @param enabled
     */
    public void setRankDirBugEnabled(boolean enabled) {
        rankDirBugEnabled = enabled;
    }

    /**
     * @see #setRankDirBugEnabled(boolean)
     */
    public boolean isRankDirBugEnabled() {
        if (rankDirBugEnabled == null)
            rankDirBugEnabled = options.remove("-rankdirbug");

        return rankDirBugEnabled;
    }

    /**
     * Look for Ruby on Rails-based naming conventions in
     * relationships between logical foreign keys and primary keys.<p>
     *
     * Basically all tables have a primary key named <code>ID</code>.
     * All tables are named plural names.
     * The columns that logically reference that <code>ID</code> are the singular
     * form of the table name suffixed with <code>_ID</code>.<p>
     *
     * @param enabled
     */
    public void setRailsEnabled(boolean enabled) {
        railsEnabled = enabled;
    }

    /**
     * @see #setRailsEnabled(boolean)
     *
     * @return
     */
    public boolean isRailsEnabled() {
        if (railsEnabled == null)
            railsEnabled = options.remove("-rails");

        return railsEnabled;
    }

    /**
     * Allow Html In Comments - encode them unless otherwise specified
     */
    public void setEncodeCommentsEnabled(boolean enabled) {
        encodeCommentsEnabled = enabled;
    }

    /**
     * @see #setEncodeCommentsEnabled(boolean)
     */
    public boolean isEncodeCommentsEnabled() {
        if (encodeCommentsEnabled == null)
            encodeCommentsEnabled = !options.remove("-ahic");

        return encodeCommentsEnabled;
    }

    /**
     * If enabled we'll attempt to query/render the number of rows that
     * each table contains.<p/>
     *
     * Defaults to <code>true</code> (enabled).
     *
     * @param enabled
     */
    public void setNumRowsEnabled(boolean enabled) {
        numRowsEnabled = enabled;
    }

    /**
     * @see #setNumRowsEnabled(boolean)
     * @return
     */
    public boolean isNumRowsEnabled() {
        if (numRowsEnabled == null)
            numRowsEnabled = !options.remove("-norows");

        return numRowsEnabled;
    }

    /**
     * If enabled we'll include views in the analysis.<p/>
     *
     * Defaults to <code>true</code> (enabled).
     *
     * @param enabled
     */
    public void setViewsEnabled(boolean enabled) {
        viewsEnabled = enabled;
    }

    /**
     * @see #setViewsEnabled(boolean)
     * @return
     */
    public boolean isViewsEnabled() {
        if (viewsEnabled == null)
            viewsEnabled = !options.remove("-noviews");

        return viewsEnabled;
    }

    /**
     * Returns <code>true</code> if metering should be embedded in
     * the generated pages.<p/>
     * Defaults to <code>false</code> (disabled).
     * @return
     */
    public boolean isMeterEnabled() {
        if (meterEnabled == null)
            meterEnabled = options.remove("-meter");

        return meterEnabled;
    }

    /**
     * Set the columns to exclude from all relationship diagrams.
     *
     * @param columnExclusions regular expression of the columns to
     *        exclude
     */
    public void setColumnExclusions(String columnExclusions) {
        this.columnExclusions = Pattern.compile(columnExclusions);
    }

    /**
     * See {@link #setColumnExclusions(String)}
     * @return
     */
    public Pattern getColumnExclusions() {
        if (columnExclusions == null) {
            String strExclusions = pullParam("-X");
            if (strExclusions == null)
                strExclusions = System.getenv("schemaspy.columnExclusions");
            if (strExclusions == null)
                strExclusions = DEFAULT_COLUMN_EXCLUSION;

            columnExclusions = Pattern.compile(strExclusions);
        }

        return columnExclusions;
    }

    /**
     * Set the columns to exclude from relationship diagrams where the specified
     * columns aren't directly referenced by the focal table.
     *
     * @param columnExclusions regular expression of the columns to
     *        exclude
     */
    public void setIndirectColumnExclusions(String fullColumnExclusions) {
        indirectColumnExclusions = Pattern.compile(fullColumnExclusions);
    }

    /**
     * @see #setIndirectColumnExclusions(String)
     *
     * @return
     */
    public Pattern getIndirectColumnExclusions() {
        if (indirectColumnExclusions == null) {
            String strExclusions = pullParam("-x");
            if (strExclusions == null)
                strExclusions = System.getenv("schemaspy.indirectColumnExclusions");
            if (strExclusions == null)
                strExclusions = DEFAULT_COLUMN_EXCLUSION;

            indirectColumnExclusions = Pattern.compile(strExclusions);
        }

        return indirectColumnExclusions;
    }

    /**
     * Set the tables to include as a regular expression
     * @param tableInclusions
     */
    public void setTableInclusions(String tableInclusions) {
        this.tableInclusions = Pattern.compile(tableInclusions);
    }

    /**
     * Get the regex {@link Pattern} for which tables to include in the analysis.
     *
     * @return
     */
    public Pattern getTableInclusions() {
        if (tableInclusions == null) {
            String strInclusions = pullParam("-i");
            if (strInclusions == null)
                strInclusions = System.getenv("schemaspy.tableInclusions");
            if (strInclusions == null)
                strInclusions = DEFAULT_TABLE_INCLUSION;

            try {
                tableInclusions = Pattern.compile(strInclusions);
            } catch (PatternSyntaxException badPattern) {
                throw new InvalidConfigurationException(badPattern).setParamName("-i");
            }
        }

        return tableInclusions;
    }

    /**
     * Set the tables to exclude as a regular expression
     * @param tableInclusions
     */
    public void setTableExclusions(String tableExclusions) {
        this.tableExclusions = Pattern.compile(tableExclusions);
    }

    /**
     * Get the regex {@link Pattern} for which tables to exclude from the analysis.
     *
     * @return
     */
    public Pattern getTableExclusions() {
        if (tableExclusions == null) {
            String strExclusions = pullParam("-I");
            if (strExclusions == null)
                strExclusions = System.getenv("schemaspy.tableExclusions");
            if (strExclusions == null)
                strExclusions = DEFAULT_TABLE_EXCLUSION;

            try {
                tableExclusions = Pattern.compile(strExclusions);
            } catch (PatternSyntaxException badPattern) {
                throw new InvalidConfigurationException(badPattern).setParamName("-I");
            }
        }

        return tableExclusions;
    }

    /**
     * @return
     */
    public List<String> getSchemas() {
        if (schemas == null) {
            String tmp = pullParam("-schemas");
            if (tmp == null)
                tmp = pullParam("-schemata");
            if (tmp != null) {
                schemas = new ArrayList<String>();

                for (String name : tmp.split("[\\s,'\"]")) {
                    if (name.length() > 0)
                        schemas.add(name);
                }

                if (schemas.isEmpty())
                    schemas = null;
            }
        }

        return schemas;
    }

    /**
     * Set the name of the {@link SqlFormatter SQL formatter} class to use to
     * format SQL into HTML.<p/>
     * The implementation of the class must be made available to the class
     * loader, typically by specifying the path to its jar with <em>-dp</em>
     * ({@link #setDriverPath(String)}).
     */
    public void setSqlFormatter(String formatterClassName) {
        sqlFormatterClass = formatterClassName;
        sqlFormatter = null;
    }

    /**
     * Set the {@link SqlFormatter SQL formatter} to use to format
     * SQL into HTML.
     */
    public void setSqlFormatter(SqlFormatter sqlFormatter) {
        this.sqlFormatter = sqlFormatter;
        if (sqlFormatter != null)
            sqlFormatterClass = sqlFormatter.getClass().getName();
    }

    /**
     * Returns an implementation of {@link SqlFormatter SQL formatter} to use to format
     * SQL into HTML.  The default implementation is {@link DefaultSqlFormatter}.
     *
     * @return
     * @throws InvalidConfigurationException if unable to instantiate an instance
     */
    @SuppressWarnings("unchecked")
    public SqlFormatter getSqlFormatter() throws InvalidConfigurationException {
        if (sqlFormatter == null) {
            if (sqlFormatterClass == null) {
                sqlFormatterClass = pullParam("-sqlFormatter");

                if (sqlFormatterClass == null)
                    sqlFormatterClass = DefaultSqlFormatter.class.getName();
            }

            try {
                Class<SqlFormatter> clazz = (Class<SqlFormatter>)Class.forName(sqlFormatterClass);
                sqlFormatter = clazz.newInstance();
            } catch (Exception exc) {
                throw new InvalidConfigurationException("Failed to initialize instance of SQL formatter: ", exc)
                            .setParamName("-sqlFormatter");
            }
        }

        return sqlFormatter;
    }

    /**
     * Set the details to show on the columns page, where "details" are
     * comma and/or space separated.
     *
     * Valid values:
     * <ul>
     * <li>id</li>
     * <li>table</li>
     * <li>column</li>
     * <li>type</li>
     * <li>size</li>
     * <li>nulls</li>
     * <li>auto</li>
     * <li>default</li>
     * <li>children</li>
     * <li>parents</li>
     * </ul>
     *
     * The default details are <code>"table column type size nulls auto default"</code>.
     * Note that "column" is the initially displayed detail and must be included.
     *
     * @param columnDetails
     */
    public void setColumnDetails(String columnDetails) {
        this.columnDetails = new ArrayList<String>();
        if (columnDetails == null || columnDetails.length() == 0) {
            // not specified, so use defaults
            columnDetails = "id table column type size nulls auto default";
        }

        for (String detail : columnDetails.split("[\\s,'\"]")) {
            if (detail.length() > 0) {
                this.columnDetails.add(detail.toLowerCase());
            }
        }

        if (!this.columnDetails.contains("column"))
            throw new InvalidConfigurationException("'column' is a required column detail");
    }

    public void setColumnDetails(List<String> columnDetails) {
        String details = columnDetails == null ? "[]" : columnDetails.toString();
        setColumnDetails(details.substring(1, details.length() - 1));
    }

    public List<String> getColumnDetails() {
        if (columnDetails == null) {
            setColumnDetails(pullParam("-columndetails"));
        }

        return columnDetails;
    }

    public void setEvaluateAllEnabled(boolean enabled) {
        evaluteAll = enabled;
    }

    public boolean isEvaluateAllEnabled() {
        if (evaluteAll == null)
            evaluteAll = options.remove("-all");
        return evaluteAll;
    }

    /**
     * Returns true if we're evaluating a bunch of schemas in one go and
     * at this point we're evaluating a specific schema.
     *
     * @return boolean
     */
    public boolean isOneOfMultipleSchemas() {
        // set by MultipleSchemaAnalyzer
        return Boolean.getBoolean("oneofmultipleschemas");
    }

    /**
     * When -all (evaluateAll) is specified then this is the regular
     * expression that determines which schemas to evaluate.
     *
     * @param schemaSpec
     */
    public void setSchemaSpec(String schemaSpec) {
        this.schemaSpec = schemaSpec;
    }

    public String getSchemaSpec() {
        if (schemaSpec == null)
            schemaSpec = pullParam("-schemaSpec");

        return schemaSpec;
    }

    /**
     * Set the renderer to use for the -Tpng[:renderer[:formatter]] dot option as specified
     * at <a href='http://www.graphviz.org/doc/info/command.html'>
     * http://www.graphviz.org/doc/info/command.html</a>.<p>
     * Note that the leading ":" is required while :formatter is optional.<p>
     * The default renderer is typically GD.<p>
     * Note that using {@link #setHighQuality(boolean)} is the preferred approach
     * over using this method.
     */
    public void setRenderer(String renderer) {
        Dot.getInstance().setRenderer(renderer);
    }

    /**
     * @see #setRenderer(String)
     * @return
     */
    public String getRenderer() {
        String renderer = pullParam("-renderer");
        if (renderer != null)
            setRenderer(renderer);

        return Dot.getInstance().getRenderer();
    }

    /**
     * If <code>false</code> then generate output of "lower quality"
     * than the default.
     * Note that the default is intended to be "higher quality",
     * but various installations of Graphviz may have have different abilities.
     * That is, some might not have the "lower quality" libraries and others might
     * not have the "higher quality" libraries.<p>
     * Higher quality output takes longer to generate and results in significantly
     * larger image files (which take longer to download / display), but it generally
     * looks better.
     */
    public void setHighQuality(boolean highQuality) {
        this.highQuality = highQuality;
        lowQuality = !highQuality;
        Dot.getInstance().setHighQuality(highQuality);
    }

    /**
     * @see #setHighQuality(boolean)
     */
    public boolean isHighQuality() {
        if (highQuality == null) {
            highQuality = options.remove("-hq");
            if (highQuality) {
                // use whatever is the default unless explicitly specified otherwise
                Dot.getInstance().setHighQuality(highQuality);
            }
        }

        highQuality = Dot.getInstance().isHighQuality();
        return highQuality;
    }

    /**
     * @see #setHighQuality(boolean)
     */
    public boolean isLowQuality() {
        if (lowQuality == null) {
            lowQuality = options.remove("-lq");
            if (lowQuality) {
                // use whatever is the default unless explicitly specified otherwise
                Dot.getInstance().setHighQuality(!lowQuality);
            }
        }

        lowQuality = !Dot.getInstance().isHighQuality();
        return lowQuality;
    }

    /**
     * Set the level of logging to perform.<p/>
     * The levels in descending order are:
     * <ul>
     *  <li><code>severe</code> (highest - least detail)
     *  <li><code>warning</code> (default)
     *  <li><code>info</code>
     *  <li><code>config</code>
     *  <li><code>fine</code>
     *  <li><code>finer</code>
     *  <li><code>finest</code>  (lowest - most detail)
     * </ul>
     *
     * @param logLevel
     */
    public void setLogLevel(String logLevel) {
        if (logLevel == null) {
            this.logLevel = Level.WARNING;
            return;
        }

        Map<String, Level> levels = new LinkedHashMap<String, Level>();
        levels.put("severe", Level.SEVERE);
        levels.put("warning", Level.WARNING);
        levels.put("info", Level.INFO);
        levels.put("config", Level.CONFIG);
        levels.put("fine", Level.FINE);
        levels.put("finer", Level.FINER);
        levels.put("finest", Level.FINEST);

        this.logLevel = levels.get(logLevel.toLowerCase());
        if (this.logLevel == null) {
            throw new InvalidConfigurationException("Invalid logLevel: '" + logLevel +
                    "'. Must be one of: " + levels.keySet());
        }
    }

    /**
     * Returns the level of logging to perform.
     * See {@link #setLogLevel(String)}.
     *
     * @return
     */
    public Level getLogLevel() {
        if (logLevel == null) {
            setLogLevel(pullParam("-loglevel"));
        }

        return logLevel;
    }

    /**
     * Returns <code>true</code> if the options indicate that the user wants
     * to see some help information.
     *
     * @return
     */
    public boolean isHelpRequired() {
        return helpRequired;
    }

    public boolean isDbHelpRequired() {
        return dbHelpRequired;
    }

    /**
     * Returns the jar that we were loaded from
     *
     * @return
     */
    public static String getLoadedFromJar() {
        String classpath = System.getProperty("java.class.path");
        return new StringTokenizer(classpath, File.pathSeparator).nextToken();
    }

    /**
     * Not a true configuration item in that it's determined at runtime
     */
    public void setHasOrphans(boolean hasOrphans) {
        this.hasOrphans = hasOrphans;
    }

    /**
     * @see #setHasOrphans()
     *
     * @return
     */
    public boolean hasOrphans() {
        return hasOrphans;
    }

    /**
     * Not a true configuration item in that it's determined at runtime
     */
    public void setHasRoutines(boolean hasRoutines) {
        this.hasRoutines = hasRoutines;
    }

    /**
     * @see #setHasRoutines()
     *
     * @return
     */
    public boolean hasRoutines() {
        return hasRoutines;
    }

    /**
     * Returns the database properties to use.
     * These should be determined by calling {@link #determineDbProperties(String)}.
     * @return
     * @throws InvalidConfigurationException
     */
    public Properties getDbProperties() throws InvalidConfigurationException {
        if (dbProperties == null) {
            try {
                dbProperties = determineDbProperties(getDbType());
            } catch (IOException exc) {
                throw new InvalidConfigurationException(exc);
            }
        }

        return dbProperties;
    }

    /**
     * Determines the database properties associated with the specified type.
     * A call to {@link #setDbProperties(Properties)} is expected after determining
     * the complete set of properties.
     *
     * @param type
     * @return
     * @throws IOException
     * @throws InvalidConfigurationException if db properties are incorrectly formed
     */
    public Properties determineDbProperties(String type) throws IOException, InvalidConfigurationException {
        ResourceBundle bundle = null;

        try {
            File propertiesFile = new File(type);
            bundle = new PropertyResourceBundle(new FileInputStream(propertiesFile));
            dbPropertiesLoadedFrom = propertiesFile.getAbsolutePath();
        } catch (FileNotFoundException notFoundOnFilesystemWithoutExtension) {
            try {
                File propertiesFile = new File(type + ".properties");
                bundle = new PropertyResourceBundle(new FileInputStream(propertiesFile));
                dbPropertiesLoadedFrom = propertiesFile.getAbsolutePath();
            } catch (FileNotFoundException notFoundOnFilesystemWithExtensionTackedOn) {
                try {
                    bundle = ResourceBundle.getBundle(type);
                    dbPropertiesLoadedFrom = "[" + getLoadedFromJar() + "]" + File.separator + type + ".properties";
                } catch (Exception notInJarWithoutPath) {
                    try {
                        String path = TableOrderer.class.getPackage().getName() + ".types." + type;
                        path = path.replace('.', '/');
                        bundle = ResourceBundle.getBundle(path);
                        dbPropertiesLoadedFrom = "[" + getLoadedFromJar() + "]/" + path + ".properties";
                    } catch (Exception notInJar) {
                        notInJar.printStackTrace();
                        notFoundOnFilesystemWithExtensionTackedOn.printStackTrace();
                        throw notFoundOnFilesystemWithoutExtension;
                    }
                }
            }
        }

        Properties props = asProperties(bundle);
        bundle = null;
        String saveLoadedFrom = dbPropertiesLoadedFrom; // keep original thru recursion

        // bring in key/values pointed to by the include directive
        // example: include.1=mysql::selectRowCountSql
        for (int i = 1; true; ++i) {
            String include = (String)props.remove("include." + i);
            if (include == null)
                break;

            int separator = include.indexOf("::");
            if (separator == -1)
                throw new InvalidConfigurationException("include directive in " + dbPropertiesLoadedFrom + " must have '::' between dbType and key");

            String refdType = include.substring(0, separator).trim();
            String refdKey = include.substring(separator + 2).trim();

            // recursively resolve the ref'd properties file and the ref'd key
            Properties refdProps = determineDbProperties(refdType);
            props.put(refdKey, refdProps.getProperty(refdKey));
        }

        // bring in base properties files pointed to by the extends directive
        String baseDbType = (String)props.remove("extends");
        if (baseDbType != null) {
            baseDbType = baseDbType.trim();
            Properties baseProps = determineDbProperties(baseDbType);

            // overlay our properties on top of the base's
            baseProps.putAll(props);
            props = baseProps;
        }

        // done with this level of recursion...restore original
        dbPropertiesLoadedFrom = saveLoadedFrom;

        // this won't be correct until the final recursion exits
        dbProperties = props;

        return props;
    }

    protected String getDbPropertiesLoadedFrom() throws IOException {
        if (dbPropertiesLoadedFrom == null)
            determineDbProperties(getDbType());
        return dbPropertiesLoadedFrom;
    }

    public List<String> getRemainingParameters()
    {
        try {
            populate();
        } catch (IllegalArgumentException exc) {
            throw new InvalidConfigurationException(exc);
        } catch (IllegalAccessException exc) {
            throw new InvalidConfigurationException(exc);
        } catch (InvocationTargetException exc) {
            if (exc.getCause() instanceof InvalidConfigurationException)
                throw (InvalidConfigurationException)exc.getCause();
            throw new InvalidConfigurationException(exc.getCause());
        } catch (IntrospectionException exc) {
            throw new InvalidConfigurationException(exc);
        }

        return options;
    }

    /**
     * Options that are specific to a type of database.  E.g. things like <code>host</code>,
     * <code>port</code> or <code>db</code>, but <b>don't</b> have a setter in this class.
     *
     * @param dbSpecificOptions
     */
    public void setDbSpecificOptions(Map<String, String> dbSpecificOptions) {
        this.dbSpecificOptions = dbSpecificOptions;
        originalDbSpecificOptions = new HashMap<String, String>(dbSpecificOptions);
    }

    public Map<String, String> getDbSpecificOptions() {
        if (dbSpecificOptions ==  null)
            dbSpecificOptions = new HashMap<String, String>();
        return dbSpecificOptions;
    }

    /**
     * Returns a {@link Properties} populated with the contents of <code>bundle</code>
     *
     * @param bundle ResourceBundle
     * @return Properties
     */
    public static Properties asProperties(ResourceBundle bundle) {
        Properties props = new Properties();
        Enumeration<String> iter = bundle.getKeys();
        while (iter.hasMoreElements()) {
            String key = iter.nextElement();
            props.put(key, bundle.getObject(key));
        }

        return props;
    }

    /**
     * 'Pull' the specified parameter from the collection of options. Returns
     * null if the parameter isn't in the list and removes it if it is.
     *
     * @param paramId
     * @return
     */
    private String pullParam(String paramId) {
        return pullParam(paramId, false, false);
    }

    private String pullRequiredParam(String paramId) {
        return pullParam(paramId, true, false);
    }

    /**
     * @param paramId
     * @param required
     * @param dbTypeSpecific
     * @return
     * @throws MissingRequiredParameterException
     */
    private String pullParam(String paramId, boolean required, boolean dbTypeSpecific)
                                throws MissingRequiredParameterException {
        int paramIndex = options.indexOf(paramId);
        if (paramIndex < 0) {
            if (required)
                throw new MissingRequiredParameterException(paramId, dbTypeSpecific);
            return null;
        }
        options.remove(paramIndex);
        String param = options.get(paramIndex).toString();
        options.remove(paramIndex);
        return param;
    }

    /**
     * Thrown to indicate that a required parameter is missing
     */
    public static class MissingRequiredParameterException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private final boolean dbTypeSpecific;

        public MissingRequiredParameterException(String paramId, boolean dbTypeSpecific) {
            this(paramId, null, dbTypeSpecific);
        }

        public MissingRequiredParameterException(String paramId, String description, boolean dbTypeSpecific) {
            super("Required parameter '" + paramId + "' " +
                    (description == null ? "" : "(" + description + ") ") +
                    "was not specified." +
                    (dbTypeSpecific ? "  It is required for this database type." : ""));
            this.dbTypeSpecific = dbTypeSpecific;
        }

        public boolean isDbTypeSpecific() {
            return dbTypeSpecific;
        }
    }

    /**
     * Allow an equal sign in args...like "-o=foo.bar". Useful for things like
     * Ant and Maven.
     *
     * @param args
     *            List
     * @return List
     */
    protected List<String> fixupArgs(List<String> args) {
        List<String> expandedArgs = new ArrayList<String>();

        for (String arg : args) {
            int indexOfEquals = arg.indexOf('=');
            if (indexOfEquals != -1 && indexOfEquals -1 != arg.indexOf(ESCAPED_EQUALS)) {
                expandedArgs.add(arg.substring(0, indexOfEquals));
                expandedArgs.add(arg.substring(indexOfEquals + 1));
            } else {
                expandedArgs.add(arg);
            }
        }

        // some OSes/JVMs do filename expansion with runtime.exec() and some don't,
        // so MultipleSchemaAnalyzer has to surround params with double quotes...
        // strip them here for the OSes/JVMs that don't do anything with the params
        List<String> unquotedArgs = new ArrayList<String>();

        for (String arg : expandedArgs) {
            if (arg.startsWith("\"") && arg.endsWith("\""))  // ".*" becomes .*
                arg = arg.substring(1, arg.length() - 1);
            unquotedArgs.add(arg);
        }

        return unquotedArgs;
    }

    /**
     * Call all the getters to populate all the lazy initialized stuff.
     *
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws IntrospectionException
     */
    private void populate() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, IntrospectionException {
        if (!populating) { // prevent recursion
            populating = true;

            BeanInfo beanInfo = Introspector.getBeanInfo(Config.class);
            PropertyDescriptor[] props = beanInfo.getPropertyDescriptors();
            for (int i = 0; i < props.length; ++i) {
                Method readMethod = props[i].getReadMethod();
                if (readMethod != null)
                    readMethod.invoke(this, (Object[])null);
            }

            populating = false;
        }
    }

    public static Set<String> getBuiltInDatabaseTypes(String loadedFromJar) {
        Set<String> databaseTypes = new TreeSet<String>();
        JarInputStream jar = null;

        try {
            jar = new JarInputStream(new FileInputStream(loadedFromJar));
            JarEntry entry;

            while ((entry = jar.getNextJarEntry()) != null) {
                String entryName = entry.getName();
                if (entryName.indexOf("types") != -1) {
                    int dotPropsIndex = entryName.indexOf(".properties");
                    if (dotPropsIndex != -1)
                        databaseTypes.add(entryName.substring(0, dotPropsIndex));
                }
            }
        } catch (IOException exc) {
        } finally {
            if (jar != null) {
                try {
                    jar.close();
                } catch (IOException ignore) {}
            }
        }

        return databaseTypes;
    }

    protected void dumpUsage(String errorMessage, boolean detailedDb) {
        if (errorMessage != null) {
            System.out.flush();
            System.err.println("*** " + errorMessage + " ***");
        } else {
            System.out.println("SchemaSpy generates an HTML representation of a database schema's relationships.");
        }

        System.err.flush();
        System.out.println();

        if (!detailedDb) {
            System.out.println("Usage:");
            System.out.println(" java -jar " + getLoadedFromJar() + " [options]");
            System.out.println("   -t databaseType       type of database - defaults to ora");
            System.out.println("                           use -dbhelp for a list of built-in types");
            System.out.println("   -u user               connect to the database with this user id");
            System.out.println("   -s schema             defaults to the specified user");
            System.out.println("   -p password           defaults to no password");
            System.out.println("   -o outputDirectory    directory to place the generated output in");
            System.out.println("   -dp pathToDrivers     optional - looks for JDBC drivers here before looking");
            System.out.println("                           in driverPath in [databaseType].properties.");
            System.out.println("Go to http://schemaspy.org for a complete list/description");
            System.out.println(" of additional parameters.");
            System.out.println();
        }

        if (detailedDb) {
            System.out.println("Built-in database types and their required connection parameters:");
            for (String type : getBuiltInDatabaseTypes(getLoadedFromJar())) {
                new DbSpecificConfig(type).dumpUsage();
            }
            System.out.println();
        }

        if (detailedDb) {
            System.out.println("You can use your own database types by specifying the filespec of a .properties file with -t.");
            System.out.println("Grab one out of " + getLoadedFromJar() + " and modify it to suit your needs.");
            System.out.println();
        }

        System.out.println("Sample usage using the default database type (implied -t ora):");
        System.out.println(" java -jar schemaSpy.jar -db mydb -s myschema -u devuser -p password -o output");
        System.out.println();
        System.out.flush();
    }

    /**
     * Get the value of the specified parameter.
     * Used for properties that are common to most db's, but aren't required.
     *
     * @param paramName
     * @return
     */
    public String getParam(String paramName) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(Config.class);
            PropertyDescriptor[] props = beanInfo.getPropertyDescriptors();
            for (int i = 0; i < props.length; ++i) {
                PropertyDescriptor prop = props[i];
                if (prop.getName().equalsIgnoreCase(paramName)) {
                    Object result = prop.getReadMethod().invoke(this, (Object[])null);
                    return result == null ? null : result.toString();
                }
            }
        } catch (Exception failed) {
            failed.printStackTrace();
        }

        return null;
    }

    /**
     * Return all of the configuration options as a List of Strings, with
     * each parameter and its value as a separate element.
     *
     * @return
     * @throws IOException
     */
    public List<String> asList() throws IOException {
        List<String> params = new ArrayList<String>();

        if (originalDbSpecificOptions != null) {
            for (String key : originalDbSpecificOptions.keySet()) {
                String value = originalDbSpecificOptions.get(key);
                if (!key.startsWith("-"))
                    key = "-" + key;
                params.add(key);
                params.add(value);
            }
        }

        if (isEncodeCommentsEnabled())
            params.add("-ahic");
        if (isEvaluateAllEnabled())
            params.add("-all");
        if (!isHtmlGenerationEnabled())
            params.add("-nohtml");
        if (!isImpliedConstraintsEnabled())
            params.add("-noimplied");
        if (!isLogoEnabled())
            params.add("-nologo");
        if (isMeterEnabled())
            params.add("-meter");
        if (!isNumRowsEnabled())
            params.add("-norows");
        if (!isViewsEnabled())
            params.add("-noviews");
        if (isRankDirBugEnabled())
            params.add("-rankdirbug");
        if (isRailsEnabled())
            params.add("-rails");
        if (isSingleSignOn())
            params.add("-sso");
        if (isSchemaDisabled())
            params.add("-noschema");

        String value = getDriverPath();
        if (value != null) {
            params.add("-dp");
            params.add(value);
        }
        params.add("-css");
        params.add(getCss());
        params.add("-charset");
        params.add(getCharset());
        params.add("-font");
        params.add(getFont());
        params.add("-fontsize");
        params.add(String.valueOf(getFontSize()));
        params.add("-t");
        params.add(getDbType());
        isHighQuality();    // query to set renderer correctly
        isLowQuality();     // query to set renderer correctly
        params.add("-renderer");  // instead of -hq and/or -lq
        params.add(getRenderer());
        value = getDescription();
        if (value != null) {
            params.add("-desc");
            params.add(value);
        }
        value = getPassword();
        if (value != null && !isPromptForPasswordEnabled()) {
            // note that we don't pass -pfp since child processes
            // won't have a console
            params.add("-p");
            params.add(value);
        }
        value = getCatalog();
        if (value != null) {
            params.add("-cat");
            params.add(value);
        }
        value = getSchema();
        if (value != null) {
            params.add("-s");
            params.add(value);
        }
        value = getUser();
        if (value != null) {
            params.add("-u");
            params.add(value);
        }
        value = getConnectionPropertiesFile();
        if (value != null) {
            params.add("-connprops");
            params.add(value);
        } else {
            Properties props = getConnectionProperties();
            if (!props.isEmpty() ) {
                params.add("-connprops");
                StringBuilder buf = new StringBuilder();
                for (Entry<Object, Object> entry : props.entrySet()) {
                    buf.append(entry.getKey());
                    buf.append(ESCAPED_EQUALS);
                    buf.append(entry.getValue());
                    buf.append(';');
                }
                params.add(buf.toString());
            }
        }
        value = getDb();
        if (value != null) {
            params.add("-db");
            params.add(value);
        }
        value = getHost();
        if (value != null) {
            params.add("-host");
            params.add(value);
        }
        if (getPort() != null) {
            params.add("-port");
            params.add(getPort().toString());
        }
        value = getServer();
        if (value != null) {
            params.add("-server");
            params.add(value);
        }
        value = getMeta();
        if (value != null) {
            params.add("-meta");
            params.add(value);
        }
        if (getGraphvizDir() != null) {
            params.add("-gv");
            params.add(getGraphvizDir().toString());
        }
        params.add("-loglevel");
        params.add(getLogLevel().toString().toLowerCase());
        params.add("-sqlFormatter");
        params.add(getSqlFormatter().getClass().getName());
        params.add("-i");
        params.add(getTableInclusions().toString());
        params.add("-I");
        params.add(getTableExclusions().toString());
        params.add("-X");
        params.add(getColumnExclusions().toString());
        params.add("-x");
        params.add(getIndirectColumnExclusions().toString());
        params.add("-dbthreads");
        params.add(String.valueOf(getMaxDbThreads()));
        params.add("-maxdet");
        params.add(String.valueOf(getMaxDetailedTables()));
        params.add("-o");
        params.add(getOutputDir().toString());

        return params;
    }
}