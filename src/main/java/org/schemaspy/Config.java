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
import java.net.URLDecoder;
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
public final class Config {

    private static final Pattern DBTYPE_PATTERN = Pattern.compile(".*org/schemaspy/types/(.*)\\.properties");

    private static Config instance;
    private final List<String> options;
    private Map<String, String> dbSpecificOptions;
    private String dbType;
    private List<String> schemas;
    private String db;
    private String host;
    private Integer port;
    private Boolean exportedKeysEnabled;
    private Pattern tableInclusions;
    private Pattern tableExclusions;
    private Pattern columnExclusions;
    private Pattern indirectColumnExclusions;
    private Integer maxDbThreads;
    private String driverPath;
    private PropertiesResolver propertiesResolver = new PropertiesResolver();
    private Properties dbProperties;
    private Boolean numRowsEnabled;
    private Boolean viewsEnabled;
    private Boolean railsEnabled;
    private Boolean evaluateAll;
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

    public String getDriverPath() {
        if (driverPath == null)
            driverPath = pullParam("-dp");

        // was previously -cp:
        if (driverPath == null)
            driverPath = pullParam("-cp");

        return driverPath;
    }

    /**
     * Maximum number of threads to use when querying database metadata information.
     * @throws InvalidConfigurationException if unable to load properties
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
     * Look for Ruby on Rails-based naming conventions in
     * relationships between logical foreign keys and primary keys.<p>
     * <p>
     * Basically all tables have a primary key named <code>ID</code>.
     * All tables are named plural names.
     * The columns that logically reference that <code>ID</code> are the singular
     * form of the table name suffixed with <code>_ID</code>.<p>
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
     */
    public boolean isViewsEnabled() {
        if (viewsEnabled == null)
            viewsEnabled = !options.remove("-noviews");

        return viewsEnabled;
    }

    /**
     * Set the columns to exclude from all relationship diagrams.
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

    public boolean isEvaluateAllEnabled() {
        if (evaluateAll == null)
            evaluateAll = options.remove("-all");
        return evaluateAll;
    }

    /**
     * When -all (evaluateAll) is specified then this is the regular
     * expression that determines which schemas to evaluate.
    */

    public String getSchemaSpec() {
        if (schemaSpec == null) {
            schemaSpec = pullParam("-schemaSpec");
            if (Objects.isNull(schemaSpec)) {
                schemaSpec = getDbProperties().getProperty("schemaSpec");
            }
            if (Objects.isNull(schemaSpec)) {
                schemaSpec = ".*";
            }
        }
        return schemaSpec;
    }

    /**
     * Returns the jar that we were loaded from
     *
     * @return jar that we have been started from
     */
    public static String getLoadedFromJar() {
        String path = Config.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        String loadedFrom = "";
        try {
            loadedFrom = URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOGGER.warn("Could not decode path {}", path);
        }

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
     * If enabled SchemaSpy will load from classpath additional jars used by JDBC Driver<p/>
     * <p>
     * Defaults to <code>false</code> (enabled).
     *
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
     *
     * @return database specific properties
     */
    public Properties getDbProperties() {
        if (dbProperties == null) {
            dbProperties = propertiesResolver.getDbProperties(getDbType());
        }
        return dbProperties;
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
     */
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
            LOGGER.info("Missing required connection parameters for '{}'", getDbProperties().getProperty("dbms"));
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
