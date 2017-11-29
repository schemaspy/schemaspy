/*
 * This file is a part of the SchemaSpy project (http://schemaspy.org).
 * Copyright (C) 2016 Rafal Kasa
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

import org.schemaspy.model.ConnectionFailure;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Driver;
import java.util.*;

/**
 * @author Rafal Kasa on 2016-08-01.
 */
public class DbDriverLoader {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private boolean loadJDBCJars = false;

    public Connection getConnection(Config config, String connectionURL,
                                       String driverClass, String driverPath) throws FileNotFoundException, IOException {
        LOGGER.info("Using database properties:");
        LOGGER.info("  {}", config.getDbPropertiesLoadedFrom());

        loadJDBCJars = config.isLoadJDBCJarsEnabled();

        Driver driver = getDriver(driverClass, driverPath);

        Properties connectionProperties = config.getConnectionProperties();
        if (config.getUser() != null) {
            connectionProperties.put("user", config.getUser());
        }
        if (config.getPassword() != null) {
            connectionProperties.put("password", config.getPassword());
        }

        Connection connection = null;
        try {
            connection = driver.connect(connectionURL, connectionProperties);
            if (connection == null) {
                System.err.println();
                System.err.println("Cannot connect to this database URL:");
                System.err.println("  " + connectionURL);
                System.err.println("with this driver:");
                System.err.println("  " + driverClass);
                System.err.println();
                System.err.println("Additional connection information may be available in ");
                System.err.println("  " + config.getDbPropertiesLoadedFrom());
                throw new ConnectionFailure("Cannot connect to '" + connectionURL +"' with driver '" + driverClass + "'");
            }
        } catch (UnsatisfiedLinkError badPath) {
            System.err.println();
            System.err.println("Failed to load driver [" + driverClass + "] from classpath " + getExistingUrls(driverPath));
            System.err.println();
            System.err.println("Make sure the reported library (.dll/.lib/.so) from the following line can be");
            System.err.println("found by your PATH (or LIB*PATH) environment variable");
            System.err.println();
            badPath.printStackTrace();
            throw new ConnectionFailure(badPath);
        } catch (Exception exc) {
            System.err.println();
            System.err.println("Failed to connect to database URL [" + connectionURL + "]");
            System.err.println();
            exc.printStackTrace();
            throw new ConnectionFailure(exc);
        }

        return connection;
    }

    /**
     * Returns an instance of {@link Driver} specified by <code>driverClass</code>
     * loaded from <code>driverPath</code>.
     *
     * @param driverClass
     * @param driverPath
     * @return
     * @throws MalformedURLException
     */
    protected Driver getDriver(String driverClass, String driverPath) throws MalformedURLException {
        Set<URL> classpath = getExistingUrls(driverPath);
        if (classpath.isEmpty()) {
            URL url = getClass().getResource(driverPath);
            if (url != null) {
                classpath = getExistingUrls(url.getPath());
            }
        }

        //If this option is true additional jars used by JDBC Driver will be loaded to the classpath
        if (loadJDBCJars) {
            loadAdditionalJarsForDriver(driverPath, classpath);
        }


        ClassLoader loader = getDriverClassLoader(classpath);
        Driver driver = null;

        try {
            driver = (Driver)Class.forName(driverClass, true, loader).newInstance();

            // have to use deprecated method or we won't see messages generated by older drivers
            //java.sql.DriverManager.setLogStream(System.err);
        } catch (Exception exc) {
            System.err.println(exc); // people don't want to see a stack trace...
            System.err.println();
            System.err.print("Failed to load driver '" + driverClass + "'");
            if (classpath.isEmpty())
                System.err.println();
            else
                System.err.println(" from: " + classpath);

            List<File> invalidClasspathEntries = getMissingFiles(driverPath);
            if (!invalidClasspathEntries.isEmpty()) {
                if (invalidClasspathEntries.size() == 1)
                    System.err.print("This entry doesn't point to a valid file/directory: ");
                else
                    System.err.print("These entries don't point to valid files/directories: ");
                System.err.println(invalidClasspathEntries);
            }
            System.err.println();
            System.err.println("Use the -dp option to specify the location of the database");
            System.err.println("drivers for your database (usually in a .jar or .zip/.Z).");
            System.err.println();
            throw new ConnectionFailure(exc);
        }

        return driver;
    }

    private void loadAdditionalJarsForDriver(String driverPath, Set<URL> classpath) throws MalformedURLException {
        File driverFolder = new File(Paths.get(driverPath).getParent().toString());
        if (driverFolder != null) {
            File[] files = driverFolder.listFiles(
                    (dir, name) -> {
                        return name.toLowerCase().matches(".*\\.?ar$");
                    }
            );

            LOGGER.info("Additional files will be loaded for JDBC Driver");

            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        classpath.add(file.toURI().toURL());
                        LOGGER.info(file.toURI().toString());
                    }
                }
            }
        }
    }

    /**
     * Returns a {@link ClassLoader class loader} to use for resolving {@link Driver}s.
     *
     * @param classpath
     * @return
     */
    private ClassLoader getDriverClassLoader(Set<URL> classpath) {
        ClassLoader loader;

        // if a classpath has been specified then use it to find the driver,
        // otherwise use whatever was used to load this class.
        // thanks to Bruno Leonardo Gonalves for this implementation that he
        // used to resolve issues when running under Maven
        if (!classpath.isEmpty()) {
            loader = new URLClassLoader(classpath.toArray(new URL[classpath.size()]));
        } else {
            loader = getClass().getClassLoader();
        }

        return loader;
    }

    /**
     * Returns a list of {@link File}s in <code>path</code> that do not exist.
     * The intent is to aid in diagnosing invalid paths.
     *
     * @param path
     * @return
     */
    private List<File> getMissingFiles(String path) {
        List<File> missingFiles = new ArrayList<File>();

        String[] pieces = path.split(File.pathSeparator);
        for (String piece : pieces) {
            File file = new File(piece);
            if (!file.exists())
                missingFiles.add(file);
        }

        return missingFiles;
    }

    /**
     * Returns a list of {@link URL}s in <code>path</code> that point to files that
     * exist.
     *
     * @param path
     * @return
     * @throws MalformedURLException
     */
    private Set<URL> getExistingUrls(String path) throws MalformedURLException {
        Set<URL> existingUrls = new HashSet<>();

        String[] pieces = path.split(File.pathSeparator);
        for (String piece : pieces) {
            File file = new File(piece);
            if (file.exists())
                existingUrls.add(file.toURI().toURL());
        }

        return existingUrls;
    }
}
