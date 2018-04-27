/*
 * Copyright (C) 2004 - 2011 John Currier
 * Copyright (C) 2017 Thomas Traude
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
package org.schemaspy.view;

import org.schemaspy.Config;
import org.schemaspy.model.InvalidConfigurationException;
import org.schemaspy.util.LineWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.*;

/**
 * Represents our CSS style sheet (CSS) with accessors for important
 * data from that style sheet.
 * The idea is that the CSS that will be used to render the HTML pages
 * also determines the colors used in the generated ER diagrams.
 *
 * @author John Currier
 * @author Thomas Traude
 * @author Daniel Watt
 */
public class StyleSheet {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static StyleSheet instance;
    private final String css;
    private String bodyBackgroundColor;
    private String tableHeadBackgroundColor;
    private String tableBackgroundColor;
    private String linkColor;
    private String linkVisitedColor;
    private String primaryKeyBackgroundColor;
    private String indexedColumnBackgroundColor;
    private String selectedTableBackgroundColor;
    private String excludedColumnBackgroundColor;
    private final List<String> ids = new ArrayList<>();

    private StyleSheet(BufferedReader cssReader) throws IOException {
        String lineSeparator = System.getProperty("line.separator");
        StringBuilder data = new StringBuilder();
        String line;

        while ((line = cssReader.readLine()) != null) {
            data.append(line);
            data.append(lineSeparator);
        }

        css = data.toString();

        int startComment = data.indexOf("/*");
        while (startComment != -1) {
            int endComment = data.indexOf("*/");
            data.replace(startComment, endComment + 2, "");
            startComment = data.indexOf("/*");
        }

        StringTokenizer tokenizer = new StringTokenizer(data.toString(), "{}");
        String id = null;
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken().trim();
            if (id == null) {
                id = token.toLowerCase();
                ids.add(id);
            } else {
                Map<String, String> attribs = parseAttributes(token);
                if (id.equals(".diagram"))
                    bodyBackgroundColor = attribs.get("background");
                else if (id.equals("th.diagram"))
                    tableHeadBackgroundColor = attribs.get("background-color");
                else if (id.equals("td.diagram"))
                    tableBackgroundColor = attribs.get("background-color");
                else if (id.equals(".diagram .primarykey"))
                    primaryKeyBackgroundColor = attribs.get("background");
                else if (id.equals(".diagram .indexedcolumn"))
                    indexedColumnBackgroundColor = attribs.get("background");
                else if (id.equals(".selectedtable"))
                    selectedTableBackgroundColor = attribs.get("background");
                else if (id.equals(".excludedcolumn"))
                    excludedColumnBackgroundColor = attribs.get("background");
                else if (id.equals("a:link"))
                    linkColor = attribs.get("color");
                else if (id.equals("a:visited"))
                    linkVisitedColor = attribs.get("color");
                id = null;
            }
        }
    }

    /**
     * Singleton accessor
     *
     * @return the singleton
     * @throws ParseException
     */
    public static StyleSheet getInstance() throws ParseException {
        if (instance == null) {
            String cssFilename = Config.getInstance().getCss();
            String templateDirectory = Config.getInstance().getTemplateDirectory();
            try {
                if (new File(cssFilename).exists()) {
                    LOGGER.info("Using external StyleSheet file: {}", cssFilename);
                    instance = new StyleSheet(new BufferedReader(MustacheWriter.getReader(null, cssFilename)));
                } else {
                    instance = new StyleSheet(new BufferedReader(MustacheWriter.getReader(templateDirectory, cssFilename)));
                }
            } catch (IOException exc) {
                throw new ParseException(exc);
            }
        }

        return instance;
    }

    private Map<String, String> parseAttributes(String data) {
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
            System.err.println("Failed to extract attributes from '" + data + "'");
            throw badToken;
        }

        return attribs;
    }

    /**
     * Write the contents of the original css to <code>out</code>.
     *
     * @param out
     * @throws IOException
     */
    public void write(LineWriter out) throws IOException {
        out.write(css);
    }

    public String getBodyBackground() {
        if (bodyBackgroundColor == null)
            throw new MissingCssPropertyException(".diagram", "background");

        return bodyBackgroundColor;
    }

    public String getTableBackground() {
        if (tableBackgroundColor == null)
            throw new MissingCssPropertyException("td", "background-color");

        return tableBackgroundColor;
    }

    public String getTableHeadBackground() {
        if (tableHeadBackgroundColor == null)
            throw new MissingCssPropertyException("th", "background-color");

        return tableHeadBackgroundColor;
    }

    public String getPrimaryKeyBackground() {
        if (primaryKeyBackgroundColor == null)
            throw new MissingCssPropertyException(".diagram .primaryKey", "background");

        return primaryKeyBackgroundColor;
    }

    public String getIndexedColumnBackground() {
        if (indexedColumnBackgroundColor == null)
            throw new MissingCssPropertyException(".diagram .indexedColumn", "background");

        return indexedColumnBackgroundColor;
    }

    public String getSelectedTableBackground() {
        if (selectedTableBackgroundColor == null)
            throw new MissingCssPropertyException(".selectedTable", "background");

        return selectedTableBackgroundColor;
    }

    public String getExcludedColumnBackgroundColor() {
        if (excludedColumnBackgroundColor == null)
            throw new MissingCssPropertyException(".excludedColumn", "background");

        return excludedColumnBackgroundColor;
    }

    public String getLinkColor() {
        if (linkColor == null)
            throw new MissingCssPropertyException("a:link", "color");

        return linkColor;
    }

    public String getLinkVisitedColor() {
        if (linkVisitedColor == null)
            throw new MissingCssPropertyException("a:visited", "color");

        return linkVisitedColor;
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
        public ParseException(Exception cause) {
            super(cause);
        }

        /**
         * @param msg textual description of the failure
         */
        public ParseException(String msg) {
            super(msg);
        }
    }
}