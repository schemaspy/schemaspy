/*
 * Copyright (c) 2018 Nils Petzaell
 *
 * This file is part of SchemaSpy.
 *
 *  SchemaSpy is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  SchemaSpy is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.schemaspy.testing;

import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.jdbc.ext.ScriptUtils;
import org.testcontainers.shaded.com.google.common.base.Charsets;
import org.testcontainers.shaded.com.google.common.io.Resources;

public class SQLScriptsRunner implements Consumer<Connection> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final String initScriptDir;
    private final String statementSeparator;

    public SQLScriptsRunner(String initScriptDir) {
        this.initScriptDir = initScriptDir;
        this.statementSeparator = ScriptUtils.DEFAULT_STATEMENT_SEPARATOR;
    }

    public SQLScriptsRunner(String initScriptDir, String statementSeparator) {
        this.initScriptDir = initScriptDir;
        this.statementSeparator = statementSeparator;
    }

    @Override
    public void accept(Connection connection) {
        try {
            List<URL> initScripts = getInitScripts();
            for (URL resource : initScripts) {
                try {
                    String sql = Resources.toString(resource, Charsets.UTF_8).replaceAll("\\r", "");
                    ScriptUtils.executeSqlScript(connection, resource.getPath(), sql, false, false, ScriptUtils.DEFAULT_COMMENT_PREFIX, statementSeparator, ScriptUtils.DEFAULT_BLOCK_COMMENT_START_DELIMITER, ScriptUtils.DEFAULT_BLOCK_COMMENT_END_DELIMITER);
                } catch (IOException | IllegalArgumentException e) {
                    LOGGER.error("Could not load classpath init script: {}", resource.getPath());
                    throw new RuntimeException("Could not load classpath init script: " + resource.getPath(), e);
                } catch (ScriptException e) {
                    LOGGER.error("Error while execution init script: {}", resource.getPath(), e);
                    throw new RuntimeException("SQLException: ", e);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Could not load classpath init scripts: {}", initScriptDir);
            throw new RuntimeException("Could not load classpath init script: " + initScriptDir, e);
        }
    }

    private List<URL> getInitScripts() throws IOException {
        List<URL> initScripts = new ArrayList<>();
        URL dir = Resources.getResource(initScriptDir);
        URLConnection urlConnection = dir.openConnection();
        if (urlConnection instanceof JarURLConnection) {
            initScripts.addAll(fromJar((JarURLConnection) urlConnection));
        } else {
            File file = new File(dir.getPath());
            if (file.isDirectory()) {
                initScripts.addAll(fromDir(file));
            } else {
                initScripts.add(file.toURI().toURL());
            }
        }

        return initScripts;
    }

    private Collection<? extends URL> fromJar(JarURLConnection jarConnection) throws IOException {
        JarFile jarFile = jarConnection.getJarFile();
        String jarConnectionEntryName = jarConnection.getEntryName();

        return jarFile.stream()
                .filter(jarEntry -> jarEntry.getName().startsWith(jarConnectionEntryName + "/") && jarEntry.getName().toLowerCase().endsWith(".sql"))
                .map(jarEntry -> {
                    try {
                        return new URL(jarEntry.getName());
                    } catch (MalformedURLException e) {
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private Collection<? extends URL> fromDir(File dir) {
        return Stream.of(dir.listFiles())
                    .filter(f -> f.getName().toLowerCase().endsWith("sql"))
                    .map(f -> {
                        try {
                            return f.toURI().toURL();
                        } catch (MalformedURLException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
    }
}
