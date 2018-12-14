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
import org.schemaspy.db.config.PropertiesResolver;
import org.schemaspy.model.InvalidConfigurationException;
import org.schemaspy.output.diagram.graphviz.GraphvizConfig;
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
public final class Config implements HtmlConfig, GraphvizConfig {

    private static final int DEFAULT_FONT_SIZE = 11;
    private static final int DEFAULT_TABLE_DETAILS_THRESHOLD = 300;
    private static final int DEFAULT_RELATION_DEGREE_DEPTH = 2;

    private static final Pattern DBTYPE_PATTERN = Pattern.compile(".*org/schemaspy/types/(.*)\\.properties");

    private static Config instance;
    private final List<String> options;
    private Map<String, String> dbSpecificOptions;
    private Map<String, String> originalDbSpecificOptions;
    private boolean helpRequired;
    private boolean dbHelpRequired;
    private String graphvizDir;
    private String dbType;
    private String schema;
    private List<String> schemas;
    private boolean oneOfMultipleSchemas;
    private String user;
    private Boolean singleSignOn;
    private String password;
    private Boolean promptForPassword;
    private String db;
    private String host;
    private Integer port;
    private String meta;
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
    private Boolean generateHtml;
    private Boolean includeImpliedConstraints;
    private Boolean rankDirBugEnabled;
    private Boolean numRowsEnabled;
    private Boolean viewsEnabled;
    private Boolean railsEnabled;
    private Boolean evaluateAll;
    private Boolean highQuality;
    private String imageFormat;
    private String renderer;
    private Boolean paginationEnabled;
    private Integer relationDegreeDepth;
    /**      
     * @deprecated replaced by -dp expanding folders
     */
    @Deprecated
    private Boolean loadJDBCJarsEnabled = false;
    private String schemaSpec;  // used in conjunction with evaluateAll
    private boolean hasOrphans;
    private boolean hasRoutines;
    private boolean populating;
    public static final String DOT_CHARSET = "UTF-8";
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
        helpRequired = options.remove("-?") ||
                options.remove("/?") ||
                options.remove("?") ||
                options.remove("-h") ||
                options.remove("-help") ||
                options.remove("--help");
        dbHelpRequired = options.remove("-dbHelp") || options.remove("-dbhelp");
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
     * @param config
     */
    public static void setInstance(Config config) {
        instance = config;
    }

    public boolean isHtmlGenerationEnabled() {
        if (generateHtml == null)
            generateHtml = !options.remove("-nohtml");

        return generateHtml;
    }

    public boolean isImpliedConstraintsEnabled() {
        if (includeImpliedConstraints == null)
            includeImpliedConstraints = !options.remove("-noimplied");

        return includeImpliedConstraints;
    }

    /**
     * Set the path to Graphviz so we can find dot to generate ER diagrams
     *
     * @param graphvizDir
     */
    public void setGraphvizDir(String graphvizDir) {
        if (graphvizDir.endsWith("\""))
            graphvizDir = graphvizDir.substring(0, graphvizDir.length() - 1);

        this.graphvizDir = new File(graphvizDir).toString();
    }

    /**
     * Return the path to Graphviz (used to find the dot executable to run to
     * generate ER diagrams).<p/>
     * <p>
     * Returns graphiz path or null
     * was not specified.
     *
     * @return
     */
    public String getGraphvizDir() {
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
     *
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
     * @return
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

    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * @deprecated use {@link CommandLineArguments#getSchema()}
     * @return
     */
    @Deprecated
    public String getSchema() {
        if (schema == null)
            schema = pullParam("-s");
        return schema;
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
     *
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return
     * @see #setPassword(String)
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
     * @param promptForPassword
     */
    public void setPromptForPasswordEnabled(boolean promptForPassword) {
        this.promptForPassword = promptForPassword;
    }

    /**
     * @return
     * @see #setPromptForPasswordEnabled(boolean)
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
     * @param propertiesFilename
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void setConnectionPropertiesFile(String propertiesFilename) throws IOException {
        if (userConnectionProperties == null)
            userConnectionProperties = new Properties();
        userConnectionProperties.load(new FileInputStream(propertiesFilename));
        userConnectionPropertiesFile = propertiesFilename;
    }

    /**
     * Returns a {@link Properties} populated either from the properties file specified
     * by {@link #setConnectionPropertiesFile(String)}, the properties specified by
     * {@link #setConnectionProperties(String)} or not populated.
     *
     * @return
     * @throws FileNotFoundException
     * @throws IOException
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
     * <p>
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
                css = "schemaSpy.css";
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
     * <p>
     * Modify the .css to specify HTML font sizes.<p>
     * <p>
     * Defaults to 11.
     *
     * @param fontSize
     */
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    /**
     * @return
     * @see #setFontSize(int)
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
     * @return
     * @see #setRelationDegreeDepth(int)
     */
    public int getRelationDegreeDepth() {
        if (relationDegreeDepth == null) {
            int iRelationDegreeDepth = DEFAULT_RELATION_DEGREE_DEPTH;
            String param = pullParam("-relationdegreedepth");
            if (param != null) {
                try {
                	iRelationDegreeDepth = Integer.parseInt(param);
                	if (iRelationDegreeDepth != 1 && iRelationDegreeDepth != 2) {
                		iRelationDegreeDepth = DEFAULT_RELATION_DEGREE_DEPTH;
                		LOGGER.warn("relationdegreedepth must be 1 or 2, set default value to 2");
                	}
                } catch (NumberFormatException e) {
                    LOGGER.warn(e.getMessage(), e);
                    LOGGER.warn("relationdegreedepth must be 1 or 2, set default value to 2");
                    iRelationDegreeDepth = DEFAULT_RELATION_DEGREE_DEPTH;
                }
            }
            relationDegreeDepth = new Integer(iRelationDegreeDepth);
        }

        return relationDegreeDepth;
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
    @Override
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
     * <p>
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
     * @return
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
     * @param enabled
     */
    public void setNumRowsEnabled(boolean enabled) {
        numRowsEnabled = enabled;
    }

    /**
     * @return
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
     * @param enabled
     */
    public void setViewsEnabled(boolean enabled) {
        viewsEnabled = enabled;
    }

    /**
     * @return
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
                throw new InvalidConfigurationException(badPattern)
                        .setParamName("-i")
                        .setParamValue(strInclusions);
            }
        }

        return tableInclusions;
    }

    /**
     * Set the tables to exclude as a regular expression
     *
     * @param tableExclusions
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
                throw new InvalidConfigurationException(badPattern)
                        .setParamName("-I")
                        .setParamValue(strExclusions);
            }
        }

        return tableExclusions;
    }

    public void setSchemas(List<String> schemas) {
        this.schemas = schemas;
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
     * @return boolean
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
        this.renderer = renderer;
    }

    /**
     * @return
     * @see #setRenderer(String)
     */
    public String getRenderer() {
        if (renderer != null) {
            renderer = pullParam("-renderer");
        }
        return renderer;
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
    }

    /**
     * @see #setHighQuality(boolean)
     */
    public boolean isHighQuality() {
        if (highQuality == null) {
            highQuality = options.remove("-hq");
        }
        return highQuality;
    }

    /**
     * @see #setHighQuality(boolean)
     */
    public boolean isLowQuality() {
        if (highQuality == null) {
            highQuality = !options.remove("-lq");
        }
        return !highQuality;
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
     * Not a true configuration item in that it's determined at runtime
     */
    public void setHasOrphans(boolean hasOrphans) {
        this.hasOrphans = hasOrphans;
    }

    /**
     * @return
     * @see #setHasOrphans(boolean)
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
     * @return
     * @see #setHasRoutines(boolean)
     */
    public boolean hasRoutines() {
        return hasRoutines;
    }

    /**
     * If enabled we'll turn on pagination in generated html<p/>
     * <p>
     * Defaults to <code>true</code> (enabled).
     *
     * @param enabled
     */
    public void setPaginationEnabled(boolean enabled) {
        paginationEnabled = enabled;
    }

    /**
     * @return
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
     * @param enabled
     * @deprecated replaced by -dp expanding folders
     */
    @Deprecated
    public void setLoadJDBCJarsEnabled(boolean enabled) {
        loadJDBCJarsEnabled = enabled;
    }

    /**
     * @return
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

    public void setImageFormat(String imageFormat) {
        this.imageFormat = imageFormat;
    }

    public String getImageFormat() {
        if (imageFormat == null) {
            imageFormat = pullParam("-imageformat");
            if (imageFormat == null)
                imageFormat = "png";
        }
        return imageFormat;
    }

    /**
     * Returns the database properties to use.
     * These should be determined by calling {@link #determineDbProperties(String)}.
     *
     * @return
     * @throws InvalidConfigurationException
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
     * @param type
     * @return
     * @throws IOException
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
     * @param dbSpecificOptions
     */
    public void setDbSpecificOptions(Map<String, String> dbSpecificOptions) {
        this.dbSpecificOptions = dbSpecificOptions;
        originalDbSpecificOptions = new HashMap<>(dbSpecificOptions);
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
            loadProperties(DEFAULT_PROPERTIES_FILE);
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
        if (Paths.get(path).toFile().exists()) {
            try (Stream<String> lineStream = Files.lines(Paths.get(path))) {
                String content = lineStream
                        .map(l -> l.replace("\\", "\\\\"))
                        .map(Config::rtrim)
                    .collect(Collectors.joining(System.lineSeparator()));
                this.schemaspyProperties.load(new StringReader(content));
            } catch (IOException e) {
                LOGGER.info("Failed to load properties", e);
            }
        } else {
            LOGGER.info("Configuration file not found");
        }
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

            while ((entry = jar.getNextJarEntry()) != null) {
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

    /**
     * Return all of the configuration options as a List of Strings, with
     * each parameter and its value as a separate element.
     *
     * @return
     * @throws IOException
     */
    public List<String> asList() throws IOException {
        List<String> params = new ArrayList<>();

        if (originalDbSpecificOptions != null) {
            for (Entry<String,String> entry : originalDbSpecificOptions.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (!key.startsWith("-"))
                    key = "-" + key;
                params.add(key);
                params.add(value);
            }
        }

        if (isEvaluateAllEnabled())
            params.add("-all");
        if (!isHtmlGenerationEnabled())
            params.add("-nohtml");
        if (!isImpliedConstraintsEnabled())
            params.add("-noimplied");
        if (!isNumRowsEnabled())
            params.add("-norows");
        if (!isViewsEnabled())
            params.add("-noviews");
        if (!isPaginationEnabled())
            params.add("-nopages");
        if (!isLoadJDBCJarsEnabled())
            params.add("-loadjars");
        if (isRankDirBugEnabled())
            params.add("-rankdirbug");
        if (isRailsEnabled())
            params.add("-rails");
        if (isSingleSignOn())
            params.add("-sso");

        String value = getDriverPath();
        if (value != null) {
            params.add("-dp");
            params.add(value);
        }
        params.add("-css");
        params.add(getCss());
        params.add("-font");
        params.add(getFont());
        params.add("-fontsize");
        params.add(String.valueOf(getFontSize()));
        params.add("-relationdegreedepth");
        params.add(String.valueOf(getRelationDegreeDepth()));
        params.add("-t");
        params.add(getDbType());
        params.add("-imageformat");
        params.add(getImageFormat());
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
            if (!props.isEmpty()) {
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
        value = getMeta();
        if (value != null) {
            params.add("-meta");
            params.add(value);
        }

        value = getTemplateDirectory();
        if (value != null) {
            params.add("-template");
            params.add(value);
        }
        if (getGraphvizDir() != null) {
            params.add("-gv");
            params.add(getGraphvizDir());
        }
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

        return params;
    }
}
