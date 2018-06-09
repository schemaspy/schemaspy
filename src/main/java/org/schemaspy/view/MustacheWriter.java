/*
 * Copyright (C) 2016, 2017 Rafal Kasa
 * Copyright (C) 2016 Ismail Simsek
 * Copyright (C) 2017 Thomas Traude
 * Copyright (C) 2017 Daniel Watt
 * Copyright (C) 2017 Nils Petzaell
 *
 * This file is part of SchemaSpy.
 *
 * SchemaSpy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SchemaSpy. If not, see <http://www.gnu.org/licenses/>.
 */
package org.schemaspy.view;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.schemaspy.Config;
import org.schemaspy.model.InvalidConfigurationException;
import org.schemaspy.util.ResourceFinder;
import org.schemaspy.util.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rkasa on 2016-03-22.
 *
 * @author Rafal Kasa
 * @author Ismail Simsek
 * @author Thomas Traude
 * @author Daniel Watt
 * @author Nils Petzaell
 */
public class MustacheWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final ResourceFinder resourceFinder = new ResourceFinder();

    private File outputDir;
    private Map<String, Object> scopes;
    private String rootPath;
    private String rootPathtoHome;
    private String databaseName;
    private boolean isMultipleSchemas;
    private String templateDirectory = Config.getInstance().getTemplateDirectory();

    public MustacheWriter(File outputDir, Map<String, Object> scopes, String rootPath, String databaseName, boolean isMultipleSchemas) {
        this.outputDir = outputDir;
        this.scopes = scopes;
        this.rootPath = rootPath;
        this.isMultipleSchemas = isMultipleSchemas;
        boolean isOneOfMultipleSchemas = Config.getInstance().isOneOfMultipleSchemas();

        if(isOneOfMultipleSchemas){
            this.rootPathtoHome = "../"+rootPath;
        }else{
            this.rootPathtoHome = rootPath;
        }
        this.databaseName = databaseName;
    }

    public void write(String templatePath, String destination, String scriptFileName) {
        MustacheFactory mf = new DefaultMustacheFactory();
        MustacheFactory contentMf = new DefaultMustacheFactory();
        StringWriter content = new StringWriter();
        StringWriter result = new StringWriter();

        HashMap<String, Object> mainScope = new HashMap<>();

        try {

            Mustache mustache = mf.compile(getReader(templatePath),"template");
            mustache.execute(result, scopes).flush();

            mainScope.put("databaseName", databaseName);
            mainScope.put("content", result);
            mainScope.put("pageScript",scriptFileName);
            mainScope.put("rootPath", rootPath);
            mainScope.put("rootPathtoHome", rootPathtoHome);
            mainScope.put("isMultipleSchemas", isMultipleSchemas);
            mainScope.putAll(scopes);

            Mustache mustacheContent = contentMf.compile(getReader("container.html"), "container");
            mustacheContent.execute(content, mainScope).flush();

            File destinationFile = new File(outputDir, destination);
            Files.write(destinationFile.toPath(), content.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            //TODO: Wouldn't we want an exception thrown here?
            LOGGER.error("Failed to write file '{}'", destination, e);
        }
    }

    private Reader getReader(String fileName) throws IOException {
        String parent = templateDirectory;
        return getReader(parent, fileName);
    }

    /**
     * Returns a {@link Reader} that can be used to read the contents
     * of the specified file in the parent directory.<p>
     * Search order is
     * <ol>
     * <li><code>fileName</code> as an explicitly-defined file in the parent directory</li>
     * <li><code>fileName</code> as a file in the user's working directory</li>
     * <li><code>fileName</code> as a file in the user's home directory</li>
     * <li><code>fileName</code> as a resource from the class path</li>
     * </ol>
     * @param parent
     * @param fileName
     *
     * @return
     * @throws IOException
     */

    public static Reader getReader(String parent, String fileName) throws IOException {
        try {
            InputStream inputStream = resourceFinder.find(parent, fileName);
            return new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        } catch (ResourceNotFoundException rnfe) {
            throw new ParseException("Unable to find requested file: " + fileName + " in directory " + parent, rnfe);
        }
    }

    /**
     * Indicates an exception in parsing the css
     */
    public static class ParseException extends InvalidConfigurationException {
        private static final long serialVersionUID = 1L;

        public ParseException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }

}
