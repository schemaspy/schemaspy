/*
 * Copyright (C) 2004 - 2011 John Currier
 * Copyright (C) 2017 Thomas Traude
 * Copyright (C) 2017 Daniel Watt
 * Copyright (c) 2018 Nils Petzaell
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
package org.schemaspy.output.dot.schemaspy;

import org.schemaspy.Config;
import org.schemaspy.model.InvalidConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import static org.schemaspy.output.dot.schemaspy.StyleSheetConst.*;

/**
 * Represents our CSS style sheet (CSS) with accessors for important
 * data from that style sheet.
 * The idea is that the CSS that will be used to render the HTML pages
 * also determines the colors used in the generated ER diagrams.
 *
 * @author John Currier
 * @author Thomas Traude
 * @author Daniel Watt
 * @author Nils Petzaell
 */
public class StyleSheet {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final ResourceFinder resourceFinder = new ResourceFinder();

    private static StyleSheet instance;
    private final String css;
    private String bodyBackgroundColor;
    private String tableHeadBackgroundColor;
    private String tableBackgroundColor;
    private String indexedColumnBackgroundColor;
    private String excludedColumnBackgroundColor;

    private StyleSheet(String cssContent) {
        css = cssContent;
        parseCss();
    }

    private void parseCss() {
        String cssNoComments = removeComments();

        StringTokenizer tokenizer = new StringTokenizer(cssNoComments, "{}");
        String id = null;
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken().trim();
            if (id == null) {
                id = token.toLowerCase();
            } else {
                Map<String, String> attribs = parseAttributes(token);
                bindAttribute(id, attribs);
                id = null;
            }
        }
    }

    private String removeComments() {
        StringBuilder data = new StringBuilder(css);

        int startComment = data.indexOf(START_COMMENT);
        while (startComment != -1) {
            int endComment = data.indexOf(END_COMMENT);
            data.replace(startComment, endComment + END_COMMENT.length(), "");
            startComment = data.indexOf(START_COMMENT);
        }

        return data.toString();
    }

    private void bindAttribute(String id, Map<String,String> attribs) {
        if (".diagram".equals(id))
            bodyBackgroundColor = attribs.get(BACKGROUND);
        else if ("th.diagram".equals(id))
            tableHeadBackgroundColor = attribs.get(BACKGROUND_COLOR);
        else if ("td.diagram".equals(id))
            tableBackgroundColor = attribs.get(BACKGROUND_COLOR);
        else if (".diagram .indexedcolumn".equals(id))
            indexedColumnBackgroundColor = attribs.get(BACKGROUND);
        else if (".excludedcolumn".equals(id))
            excludedColumnBackgroundColor = attribs.get(BACKGROUND);
    }

    /**
     * Singleton accessor
     *
     * @return the singleton
     * @throws ParseException
     */
    public static StyleSheet getInstance() {
        if (instance == null) {
            String cssFilename = Config.getInstance().getCss();
            String templateDirectory = Config.getInstance().getTemplateDirectory();
            try {
                if (new File(cssFilename).exists()) {
                    LOGGER.info("Using external StyleSheet file: {}", cssFilename);
                    instance = new StyleSheet(getContent(getReader(null, cssFilename)));
                } else {
                    instance = new StyleSheet(getContent(getReader(templateDirectory, cssFilename)));
                }
            } catch (IOException exc) {
                throw new ParseException("Unable to find css '" + cssFilename + "' or same file in '" + templateDirectory + "'" , exc);
            }
        }

        return instance;
    }

    private static String getContent(Reader reader) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(reader);
        String lineSeparator = System.getProperty("line.separator");
        StringBuilder data = new StringBuilder();
        String line;

        while ((line = bufferedReader.readLine()) != null) {
            data.append(line);
            data.append(lineSeparator);
        }

        return data.toString();
    }

    private static Reader getReader(String parent, String fileName) {
        try {
            InputStream inputStream = resourceFinder.find(parent, fileName);
            return new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        } catch (ResourceNotFoundException rnfe) {
            throw new ParseException("Unable to find requested file: " + fileName + " in directory " + parent, rnfe);
        }
    }

    private static Map<String, String> parseAttributes(String data) {
        Map<String, String> attribs = new HashMap<>();

        try {
            StringTokenizer attrTokenizer = new StringTokenizer(data, ";");
            while (attrTokenizer.hasMoreTokens()) {
                StringTokenizer pairTokenizer = new StringTokenizer(attrTokenizer.nextToken(), ":");
                String attribute = pairTokenizer.nextToken().trim().toLowerCase();
                String value = pairTokenizer.nextToken().trim().toLowerCase();
                attribs.put(attribute, value);
            }
        } catch (NoSuchElementException badToken) {
            LOGGER.warn("Failed to extract attributes from '{}'", data, badToken);
            throw badToken;
        }

        return attribs;
    }

    public String getBodyBackground() {
        if (bodyBackgroundColor == null)
            throw new MissingCssPropertyException(".diagram", BACKGROUND);

        return bodyBackgroundColor;
    }

    public String getTableBackground() {
        if (tableBackgroundColor == null)
            throw new MissingCssPropertyException("td", BACKGROUND_COLOR);

        return tableBackgroundColor;
    }

    public String getTableHeadBackground() {
        if (tableHeadBackgroundColor == null)
            throw new MissingCssPropertyException("th", BACKGROUND_COLOR);

        return tableHeadBackgroundColor;
    }

    public String getIndexedColumnBackground() {
        if (indexedColumnBackgroundColor == null)
            throw new MissingCssPropertyException(".diagram .indexedColumn", BACKGROUND);

        return indexedColumnBackgroundColor;
    }


    public String getExcludedColumnBackgroundColor() {
        if (excludedColumnBackgroundColor == null)
            throw new MissingCssPropertyException(".excludedColumn", BACKGROUND);

        return excludedColumnBackgroundColor;
    }



    /**
     * Indicates that a css property was missing
     */
    public static class MissingCssPropertyException extends InvalidConfigurationException {
        private static final long serialVersionUID = 1L;

        /**
         * @param cssSection name of the css section
         * @param propName name of the missing property in that section
         */
        public MissingCssPropertyException(String cssSection, String propName) {
            super("Required property '" + propName + "' was not found for the definition of '" + cssSection + "'");
        }
    }

    /**
     * Indicates an exception in parsing the css
     */
    public static class ParseException extends InvalidConfigurationException {
        private static final long serialVersionUID = 1L;

        /**
         * @param cause root exception that caused the failure
         */
        public ParseException(String message, Exception cause) {
            super(message, cause);
        }
    }
}