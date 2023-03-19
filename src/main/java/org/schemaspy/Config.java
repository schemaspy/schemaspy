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

import org.schemaspy.input.dbms.config.PropertiesResolver;
import org.schemaspy.input.dbms.config.SimplePropertiesResolver;
import org.schemaspy.model.InvalidConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
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

    private static Config instance;
    private final List<String> options;
    private String dbType;
    private String db;
    private Boolean exportedKeysEnabled;
    private Pattern tableInclusions;
    private Pattern tableExclusions;
    private Pattern columnExclusions;
    private Pattern indirectColumnExclusions;
    private Integer maxDbThreads;
    private PropertiesResolver propertiesResolver = new SimplePropertiesResolver();
    private Properties dbProperties;
    private Boolean numRowsEnabled;
    private Boolean viewsEnabled;
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

    /**
     * @return Name of database as supplied with -db or set during multi schema analysis
     */
    public String getDb() {
        if (db == null)
            db = pullParam("-db");
        return db;
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

    /**
     * 'Pull' the specified parameter from the collection of options. Returns
     * null if the parameter isn't in the list and removes it if it is.
     *
     * @param paramId argument name to fetch
     * @return the value for paramId
     */
    private String pullParam(String paramId) {
        int paramIndex = options.indexOf(paramId);
        if (paramIndex < 0) {
            return null;
        }
        options.remove(paramIndex);
        String param = options.get(paramIndex);
        options.remove(paramIndex);
        return param;
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

}
