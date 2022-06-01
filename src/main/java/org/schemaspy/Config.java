/*
 * Copyright (C) 2004-2011 John Currier
 * Copyright (C) 2016, 2017 Rafal Kasa
 * Copyright (C) 2016 Ismail Simsek
 * Copyright (C) 2017 Wojciech Kasa
 * Copyright (C) 2017 Thomas Traude
 * Copyright (C) 2017, 2018 Nils Petzaell
 * Copyright (C) 2017 Daniel Watt
 *
 * This file is a part of the SchemaSpy project (http://schemaspy.org).
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

import org.schemaspy.cli.CommandLineArgumentParser;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.input.dbms.config.PropertiesResolver;
import org.schemaspy.model.InvalidConfigurationException;
import org.schemaspy.util.DbSpecificConfig;
import org.schemaspy.view.HtmlConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Configuration of a SchemaSpy run
 *
 * @author John Currier
 * @author Rafal Kasa
 * @author Ismail Simsek
 * @author Wojciech Kasa
 * @author Thomas Traude
 * @author Nils Petzaell
 * @author Daniel Watt
 */

/**
 * See https://github.com/schemaspy/schemaspy/projects/3
 * @deprecated Move to jCommander
 */
@Deprecated
public final class Config implements HtmlConfig {

    private static final int DEFAULT_FONT_SIZE = 11;
    private static final int DEFAULT_TABLE_DETAILS_THRESHOLD = 300;

    private static final Pattern DBTYPE_PATTERN = Pattern.compile(".*org/schemaspy/types/(.*)\\.properties");

    private static Config instance;
    private final List<String> options;
    private Map<String, String> dbSpecificOptions;
    private String dbType;
    private List<String> schemas;
    private boolean oneOfMultipleSchemas;
    private String user;
    private Boolean singleSignOn;
    private String password;
    private Boolean promptForPassword;
    private String db;
    private String host;
    private Integer port;
    private Boolean exportedKeysEnabled;
    private String templateDirectory;
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
    private String font;
    private Integer fontSize;
    private String description;
    private PropertiesResolver propertiesResolver = new PropertiesResolver();
    private Properties dbProperties;
    private Boolean rankDirBugEnabled;
    private Boolean numRowsEnabled;
    private Boolean viewsEnabled;
    private Boolean railsEnabled;
    private Boolean evaluateAll;
    private Boolean paginationEnabled;
    /**
     * @deprecated replaced by -dp expanding folders
     */
    @Deprecated
    private Boolean loadJDBCJarsEnabled = false;
    private String schemaSpec;  // used in conjunction with evaluateAll
    private boolean populating;
    private static final String ESCAPED_EQUALS = "\\=";
    private static final String DEFAULT_TABLE_INCLUSION = ".*"; // match everything
    private static final String DEFAULT_TABLE_EXCLUSION = ".*\\$.*";
    private static final String DEFAULT_COLUMN_EXCLUSION = "[^.]";  // match nothing
    private static final String DEFAULT_PROPERTIES_FILE = "schemaspy.properties";
    private Properties schemaspyProperties = new Properties();
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Default constructor. Intended for when you want to inject properties
     * independently (i.e. not from a command line interface).
     */
    public Config() {
        if (instance == null)
            setInstance(this);
        options = new ArrayList<>();
    }

    /**
     * Construct a configuration from an array of options (e.g. from a command
     * line interface).
     *
     * @param argv
     */
    public Config(String... argv) {
        setInstance(this);
        options = fixupArgs(Arrays.asList(argv));
    }

    public static Config getInstance() {
        if (instance == null)
            instance = new Config();

        return instance;
    }

    /**
     * Sets the global instance.
     * <p>
     * Useful for things like selecting a specific configuration in a UI.
     *
     * @param config existing och created config object
     */
    public static void setInstance(Config config) {
        instance = config;
    }

    public boolean isExportedKeysEnabled() {
        if (Objects.isNull(exportedKeysEnabled)) {
            exportedKeysEnabled = !options.remove("-noexportedkeys");
        }
        return exportedKeysEnabled;
    }

    public String getTemplateDirectory() {
        if (templateDirectory == null) {
            templateDirectory = pullParam("-template");
            if (templateDirectory == null) {
                templateDirectory = "layout";
            }
        }
        return templateDirectory;
    }

    /**
     * @deprecated use {@link CommandLineArguments#getDatabaseType()}
     * @return databaseType as supplied with -t if null returns "ora"
     */
    @Deprecated
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

    /**
     * @return Name of database as supplied with -db or set during multi schema analysis
     */
    public String getDb() {
        if (db == null)
            db = pullParam("-db");
        return db;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        if (host == null)
            host = pullParam("-host");
        return host;
    }

    public Integer getPort() {
        if (port == null) {
            String portAsString = pullParam("-port");
            if (hasText(portAsString)) {
                try {
                    port = Integer.valueOf(portAsString);
                } catch (NumberFormatException notSpecified) {
                    LOGGER.warn(notSpecified.getMessage(), notSpecified);
                }
            }
        }
        return port;
    }

    private static boolean hasText(String string) {
        return Objects.nonNull(string) && !string.trim().isEmpty();
    }

    public void setUser(String user) {
        this.user = user;
    }

    /**
     * User used to connect to the database.
     * Required unless single sign-on is enabled
     * (see {@link #setSingleSignOn(boolean)}).
     *
     * @return user as supplied with -u
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
     *
     * @return true if single sign-on else false
     */
    public boolean isSingleSignOn() {
        if (singleSignOn == null)
            singleSignOn = options.remove("-sso");

        return singleSignOn;
    }

    /**
     * Set the password used to connect to the database.
     *
     * @param password db user password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @see #setPassword(String)
     *
     * @return get password as set by -p or with setPassword
     */
    public String getPassword() {
        if (password == null)
            password = pullParam("-p");

        if (password == null && isPromptForPasswordEnabled()) {
            Console console = System.console();
            if (Objects.isNull(console)) {
                LOGGER.error("No console found for password input");
            } else {
                password = new String(console.readPassword("Password: "));
            }
        }

        if (password == null) {
            password = System.getenv("schemaspy.pw");
        }

        return password;
    }

    /**
     * Set to <code>true</code> to prompt for the password
     *
     * @param promptForPassword should we prompt for a password.
     */
    public void setPromptForPasswordEnabled(boolean promptForPassword) {
        this.promptForPassword = promptForPassword;
    }

    /**
     * @see #setPromptForPasswordEnabled(boolean)
     *
     * @return true if we should prompt for password
     */

    public boolean isPromptForPasswordEnabled() {
        if (promptForPassword == null) {
            promptForPassword = options.remove("-pfp");
        }

        return promptForPassword;
    }

    public void setMaxDetailedTabled(int maxDetailedTables) {
        this.maxDetailedTables = maxDetailedTables;
    }

    public int getMaxDetailedTables() {
        if (maxDetailedTables == null) {
            int max = DEFAULT_TABLE_DETAILS_THRESHOLD;
            String param = pullParam("-maxdet");
            if (param != null) {
                try {
                    max = Integer.parseInt(param);
                } catch (NumberFormatException e) {
                    LOGGER.warn(e.getMessage(), e);
                }
            }
            maxDetailedTables = max;
        }

        return maxDetailedTables;
    }

    public String getConnectionPropertiesFile() {
        return userConnectionPropertiesFile;
    }

    /**
     * Properties from this file (in key=value pair format) are passed to the
     * database connection.<br>
     * user (from -u) and password (from -p) will be passed in the
     * connection properties if specified.
     *
     * @param propertiesFilename file to use for connection properties
     * @throws IOException if we have problems reading the file
     */
    public void setConnectionPropertiesFile(String propertiesFilename) throws IOException {
        if (userConnectionProperties == null)
            userConnectionProperties = new Properties();
        try (InputStream inputStream = new FileInputStream(propertiesFilename)) {
            userConnectionProperties.load(inputStream);
        }
        userConnectionPropertiesFile = propertiesFilename;
    }

    /**
     * Returns a {@link Properties} populated either from the properties file specified
     * by {@link #setConnectionPropertiesFile(String)}, the properties specified by
     * {@link #setConnectionProperties(String)} or not populated.
     *
     * @return connection properties to use when connecting
     * @throws IOException if we a have problems reading the properties file if -connprops is a file
     */
    public Properties getConnectionProperties() throws IOException {
        if (userConnectionProperties == null) {
            String props = pullParam("-connprops");
            if (props != null) {
                if (props.contains(ESCAPED_EQUALS)) {
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
     * @param properties string with key\\=value pairs separated by ; of connection properties
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
     * <p>
     * Defaults to <code>"schemaSpy.css"</code>.
     *
     * @param css file path for custom css-file
     */
    public void setCss(String css) {
        this.css = css;
    }

    public String getCss() {
        if (css == null) {
            css = pullParam("-css");
            if (css == null)
                css = "schemaSpy.css";
        }
        return css;
    }

    /**
     * The font to use within diagrams.  Modify the .css to specify HTML fonts.
     *
     * @param font font name to use
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
     * 'large' (e.g. not 'compact') diagrams.
     *
     * Modify the .css to specify HTML font sizes.
     *
     * Defaults to 11.
     *
     * @param fontSize set the size of the font, default 11.
     */
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    /**
     * @see #setFontSize(int)
     * @return the font size to use
     */
    public int getFontSize() {
        if (fontSize == null) {
            int size = DEFAULT_FONT_SIZE;
            String param = pullParam("-fontsize");
            if (param != null) {
                try {
                    size = Integer.parseInt(param);
                } catch (NumberFormatException e) {
                    LOGGER.warn(e.getMessage(), e);
                }
            }
            fontSize = size;
        }

        return fontSize;
    }

    /**
     * Description of schema that gets display on main pages.
     *
     * @param description description to put into html report
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see #setDescription(String)
     */
    @Override
    public String getDescription() {
        if (description == null)
            description = pullParam("-desc");
        return description;
    }

    /**
     * Maximum number of threads to use when querying database metadata information.
     *
     * @param maxDbThreads change the number of threads used to fetch data from db
     */
    public void setMaxDbThreads(int maxDbThreads) {
        this.maxDbThreads = maxDbThreads;
    }

    /**
     * @throws InvalidConfigurationException if unable to load properties
     * @see #setMaxDbThreads(int)
     */
    public int getMaxDbThreads() {
        if (maxDbThreads == null) {
            Properties properties = getDbProperties();

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

            maxDbThreads = max;
        }

        return maxDbThreads;
    }

    /**
     * Don't use this unless absolutely necessary as it screws up the layout
     *
     * @param enabled should we skip setting RIGHT to LEFT
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
     * <p>
     * Basically all tables have a primary key named <code>ID</code>.
     * All tables are named plural names.
     * The columns that logically reference that <code>ID</code> are the singular
     * form of the table name suffixed with <code>_ID</code>.<p>
     *
     * @param enabled if we should use rails naming convention
     */
    public void setRailsEnabled(boolean enabled) {
        railsEnabled = enabled;
    }

    /**
     * @return if we should use rails naming convention
     * @see #setRailsEnabled(boolean)
     */
    public boolean isRailsEnabled() {
        if (railsEnabled == null)
            railsEnabled = options.remove("-rails");

        return railsEnabled;
    }

    /**
     * If enabled we'll attempt to query/render the number of rows that
     * each table contains.<p/>
     * <p>
     * Defaults to <code>true</code> (enabled).
     *
     * @param enabled should we show number of rows
     */
    public void setNumRowsEnabled(boolean enabled) {
        numRowsEnabled = enabled;
    }

    /**
     * @return if we should show number of rows
     * @see #setNumRowsEnabled(boolean)
     */
    public boolean isNumRowsEnabled() {
        if (numRowsEnabled == null)
            numRowsEnabled = !options.remove("-norows");

        return numRowsEnabled;
    }

    /**
     * If enabled we'll include views in the analysis.<p/>
     * <p>
     * Defaults to <code>true</code> (enabled).
     *
     * @param enabled should we process views
     */
    public void setViewsEnabled(boolean enabled) {
        viewsEnabled = enabled;
    }

    /**
     * @return if we should process views
     * @see #setViewsEnabled(boolean)
     */
    public boolean isViewsEnabled() {
        if (viewsEnabled == null)
            viewsEnabled = !options.remove("-noviews");

        return viewsEnabled;
    }

    /**
     * Set the columns to exclude from all relationship diagrams.
     *
     * @param columnExclusions regular expression of the columns to
     *                         exclude
     */
    public void setColumnExclusions(String columnExclusions) {
        this.columnExclusions = Pattern.compile(columnExclusions);
    }

    /**
     * See {@link #setColumnExclusions(String)}
     *
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
     * @param fullColumnExclusions regular expression of the columns to
     *                         exclude
     */
    public void setIndirectColumnExclusions(String fullColumnExclusions) {
        indirectColumnExclusions = Pattern.compile(fullColumnExclusions);
    }

    /**
     * @return
     * @see #setIndirectColumnExclusions(String)
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
     *
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
                throw new InvalidConfigurationException(badPattern, "-i", strInclusions);
            }
        }

        return tableInclusions;
    }

    /**
     * Set the tables to exclude as a regular expression
     *
     * @param tableExclusions tables to exclude
     */
    public void setTableExclusions(String tableExclusions) {
        this.tableExclusions = Pattern.compile(tableExclusions);
    }

    /**
     * Get the regex {@link Pattern} for which tables to exclude from the analysis.
     *
     * @return table exclusions
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
                throw new InvalidConfigurationException(badPattern, "-I", strExclusions);
            }
        }

        return tableExclusions;
    }

    public void setSchemas(List<String> schemas) {
        this.schemas = schemas;
    }

    /**
     * @return list of schemas to process
     */
    public List<String> getSchemas() {
        if (schemas == null) {
            String tmp = pullParam("-schemas");
            if (tmp == null)
                tmp = pullParam("-schemata");
            if (tmp != null) {
                schemas = new ArrayList<>();

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

    public void setEvaluateAllEnabled(boolean enabled) {
        evaluateAll = enabled;
    }

    public boolean isEvaluateAllEnabled() {
        if (evaluateAll == null)
            evaluateAll = options.remove("-all");
        return evaluateAll;
    }

    /**
     * Returns true if we're evaluating a bunch of schemas in one go and
     * at this point we're evaluating a specific schema.
     *
     * @return boolean if we are processing multiple schemas
     */
    @Override
    public boolean isOneOfMultipleSchemas() {
        return oneOfMultipleSchemas;
    }

    public void setOneOfMultipleSchemas(boolean oneOfMultipleSchemas) {
        // set by SchemaAnalyzer.analyzeMultipleSchemas function.
        this.oneOfMultipleSchemas = oneOfMultipleSchemas;
    }

    /**
     * When -all (evaluateAll) is specified then this is the regular
     * expression that determines which schemas to evaluate.
     *
     * @param schemaSpec used to filter when -all is used (regex)
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
     * Returns the jar that we were loaded from
     *
     * @return jar that we have been started from
     */
    public static String getLoadedFromJar() {
        String loadedFrom = Config.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        if (loadedFrom.contains("!/BOOT-INF")) {
            try {
                loadedFrom = new URL(loadedFrom).getFile();
                loadedFrom = loadedFrom.substring(0, loadedFrom.indexOf('!'));
            } catch (MalformedURLException e) {
                String classpath = System.getProperty("java.class.path");
                return new StringTokenizer(classpath, File.pathSeparator).nextToken();
            }
        }
        return loadedFrom;
    }

    /**
     * If enabled we'll turn on pagination in generated html<p/>
     * <p>
     * Defaults to <code>true</code> (enabled).
     *
     * @param enabled if we should use pagination
     */
    public void setPaginationEnabled(boolean enabled) {
        paginationEnabled = enabled;
    }

    /**
     * @return if pagination is enabled
     * @see #setPaginationEnabled(boolean)
     */
    @Override
    public boolean isPaginationEnabled() {
        if (paginationEnabled == null)
            paginationEnabled = !options.remove("-nopages");

        return paginationEnabled;
    }


    /**
     * If enabled SchemaSpy will load from classpath additional jars used by JDBC Driver<p/>
     * <p>
     * Defaults to <code>false</code> (enabled).
     *
     * @param enabled if we should add sibling libraries when -dp is specified
     * @deprecated replaced by -dp expanding folders
     */
    @Deprecated
    public void setLoadJDBCJarsEnabled(boolean enabled) {
        loadJDBCJarsEnabled = enabled;
    }

    /**
     * @return if should are loading sibling libraries
     * @see #setLoadJDBCJarsEnabled(boolean)
     * @deprecated replaced by -dp expanding folders
     */
    @Deprecated
    public boolean isLoadJDBCJarsEnabled() {
        String loadJars = pullParam("-loadjars");
        if (loadJars != null && "true".equals(loadJars)) {
            loadJDBCJarsEnabled = true;
        }

        return loadJDBCJarsEnabled;
    }

    /**
     * Returns the database properties to use.
     * These should be determined by calling {@link #determineDbProperties(String)}.
     *
     * @return database specific properties
     */
    public Properties getDbProperties() {
        if (dbProperties == null) {
            dbProperties = propertiesResolver.getDbProperties(getDbType());
        }
        return dbProperties;
    }

    /**
     * Determines the database properties associated with the specified type.
     *
     * @param type database type that should be resolved
     * @return resolved properties for database type
     * @throws InvalidConfigurationException if db properties are incorrectly formed
     */
    public Properties determineDbProperties(String type) {
        return propertiesResolver.getDbProperties(type);
    }

    public List<String> getRemainingParameters() {
        try {
            populate();
        } catch (IllegalArgumentException |
                 IllegalAccessException |
                 IntrospectionException exc) {
            throw new InvalidConfigurationException(exc);
        } catch (InvocationTargetException exc) {
            if (exc.getCause() instanceof InvalidConfigurationException)
                throw (InvalidConfigurationException) exc.getCause();
            throw new InvalidConfigurationException(exc.getCause());
        }

        return options;
    }

    /**
     * Options that are specific to a type of database.  E.g. things like <code>host</code>,
     * <code>port</code> or <code>db</code>, but <b>don't</b> have a setter in this class.
     *
     * @param dbSpecificOptions options related to database type
     */
    public void setDbSpecificOptions(Map<String, String> dbSpecificOptions) {
        this.dbSpecificOptions = dbSpecificOptions;
    }

    public Map<String, String> getDbSpecificOptions() {
        if (dbSpecificOptions == null)
            dbSpecificOptions = new HashMap<>();
        return dbSpecificOptions;
    }

    /**
     * 'Pull' the specified parameter from the collection of options. Returns
     * null if the parameter isn't in the list and removes it if it is.
     *
     * @param paramId argument name to fetch
     * @return the value for paramId
     */
    private String pullParam(String paramId) {
        return pullParam(paramId, false, false);
    }

    private String pullRequiredParam(String paramId) {
        return pullParam(paramId, true, false);
    }

    /**
     * @param paramId argument name to fetch
     * @param required if argument must be specified
     * @param dbTypeSpecific if argument is database type specific
     * @return value for argument
     * @throws MissingRequiredParameterException if argument is required and not specified
     */
    private String pullParam(String paramId, boolean required, boolean dbTypeSpecific) {
        int paramIndex = options.indexOf(paramId);
        if (paramIndex < 0) {
            if (required)
                throw new MissingRequiredParameterException(paramId, dbTypeSpecific);
            return null;
        }
        options.remove(paramIndex);
        String param = options.get(paramIndex);
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
     * @param args List
     * @return List
     */
    private List<String> fixupArgs(List<String> args) {
        List<String> expandedArgs = new ArrayList<>();

        for (String arg : args) {
            int indexOfEquals = arg.indexOf('=');
            if (indexOfEquals != -1 && indexOfEquals - 1 != arg.indexOf(ESCAPED_EQUALS)) {
                expandedArgs.add(arg.substring(0, indexOfEquals));
                expandedArgs.add(arg.substring(indexOfEquals + 1));
            } else {
                expandedArgs.add(arg);
            }
        }
        if (expandedArgs.indexOf("-configFile") < 0) {
            if (fileExists(DEFAULT_PROPERTIES_FILE)) {
                loadProperties(DEFAULT_PROPERTIES_FILE);
            }
        } else {
            loadProperties(expandedArgs.get(expandedArgs.indexOf("-configFile") + 1));
        }
        for (Entry<Object, Object> prop : schemaspyProperties.entrySet()) {
            if (!expandedArgs.contains(prop.getKey().toString().replace("schemaspy.", "-"))) {
                expandedArgs.add(prop.getKey().toString().replace("schemaspy.", "-"));
                expandedArgs.add(prop.getValue().toString());
            }
        }
        // some OSes/JVMs do filename expansion with runtime.exec() and some don't,
        // so MultipleSchemaAnalyzer has to surround params with double quotes...
        // strip them here for the OSes/JVMs that don't do anything with the params
        List<String> unquotedArgs = new ArrayList<>();

        for (String arg : expandedArgs) {
            if (arg.startsWith("\"") && arg.endsWith("\""))  // ".*" becomes .*
                arg = arg.substring(1, arg.length() - 1);
            unquotedArgs.add(arg);
        }

        return unquotedArgs;
    }

    private void loadProperties(String path) {
        if (fileExists(path)) {
            try (Stream<String> lineStream = Files.lines(Paths.get(path))) {
                String content = lineStream
                        .map(l -> l.replace("\\", "\\\\"))
                        .map(Config::rtrim)
                    .collect(Collectors.joining(System.lineSeparator()));
                this.schemaspyProperties.load(new StringReader(content));
                LOGGER.info("Loaded configuration from {}", path);
            } catch (IOException e) {
                LOGGER.warn("Failed to load configuration from '{}", path, e);
            }
        } else {
            LOGGER.warn("Configuration file '{}' not found", path);
        }
    }

    private static boolean fileExists(String path) {
        return Paths.get(path).toFile().exists();
    }

    private static String rtrim(String s) {
        int i = s.length()-1;
        while (i >= 0 && Character.isWhitespace(s.charAt(i))) {
            i--;
        }
        return s.substring(0,i+1);
    }

    /**
     * Call all the getters to populate all the lazy initialized stuff.
     *
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws IntrospectionException
     */
    private void populate() throws IllegalAccessException, InvocationTargetException, IntrospectionException {
        if (!populating) { // prevent recursion
            populating = true;

            BeanInfo beanInfo = Introspector.getBeanInfo(Config.class);
            PropertyDescriptor[] props = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor prop : props) {
                Method readMethod = prop.getReadMethod();
                if (readMethod != null)
                    readMethod.invoke(this, (Object[]) null);
            }

            populating = false;
        }
    }

    public static Set<String> getBuiltInDatabaseTypes(String loadedFromJar) {
        Set<String> databaseTypes = new TreeSet<>();
        try (JarInputStream jar = new JarInputStream(new FileInputStream(loadedFromJar))){
            JarEntry entry;

            while ((entry = jar.getNextJarEntry()) != null) { //NOSONAR
                Matcher dbTypeMatcher = DBTYPE_PATTERN.matcher(entry.getName());
                if (dbTypeMatcher.find()) {
                    databaseTypes.add(dbTypeMatcher.group(1));
                }
            }
        } catch (IOException exc) {
            LOGGER.error("Failed to read bundled DatabaseTypes", exc);
        }

        return databaseTypes;
    }

    /**
     * @deprecated use {@link CommandLineArgumentParser#printUsage()}
     * @param errorMessage
     * @param detailedDb
     */
    @Deprecated
    void dumpUsage(String errorMessage, boolean detailedDb) {

        if (errorMessage != null) {
            LOGGER.error("*** {} ***", errorMessage );
        } else {
            LOGGER.info("SchemaSpy generates an HTML representation of a database schema's relationships.");
        }


        if (!detailedDb) {
            LOGGER.info("Usage:");
            LOGGER.info(" java -jar {} [options]", getLoadedFromJar());
            LOGGER.info("   -t databaseType       type of database - defaults to ora");
            LOGGER.info("                           use -dbhelp for a list of built-in types");
            LOGGER.info("   -u user               connect to the database with this user id");
            LOGGER.info("   -s schema             defaults to the specified user");
            LOGGER.info("   -p password           defaults to no password");
            LOGGER.info("   -o outputDirectory    directory to place the generated output in");
            LOGGER.info("   -dp pathToDrivers     optional - looks for JDBC drivers here before looking");
            LOGGER.info("                           in driverPath in [databaseType].properties.");
            LOGGER.info("Go to http://schemaspy.org for a complete list/description");
            LOGGER.info(" of additional parameters.");
        }

        if (detailedDb) {
            LOGGER.info("Missing required connection parameters for '{}'", getDbType());
            new DbSpecificConfig(getDbType(), getDbProperties()).dumpUsage();
        }

        if (detailedDb) {
            LOGGER.info("You can use your own database types by specifying the filespec of a .properties file with -t.");
            LOGGER.info("Grab one out of {} and modify it to suit your needs.", getLoadedFromJar());
        }

        LOGGER.info("Sample usage using the default database type (implied -t ora):");
        LOGGER.info(" java -jar schemaSpy.jar -db mydb -s myschema -u devuser -p password -o output");
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
            for (PropertyDescriptor prop : props) {
                if (prop.getName().equalsIgnoreCase(paramName)) {
                    Object result = prop.getReadMethod().invoke(this, (Object[]) null);
                    return result == null ? null : result.toString();
                }
            }
        } catch (Exception failed) {
            LOGGER.error("Unable to get parameter {}",paramName,failed);
        }

        return null;
    }
}
