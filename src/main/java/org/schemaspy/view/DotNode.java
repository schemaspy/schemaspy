/*
 * Copyright (C) 2004 - 2011 John Currier
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2017 Wojciech Kasa
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

import com.github.mustachejava.util.HtmlEscaper;
import org.schemaspy.Config;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.schemaspy.model.TableIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author John Currier
 * @author Rafal Kasa
 * @author Wojciech Kasa
 * @author Thomas Traude
 * @author Daniel Watt
 * @author Nils Petzaell
 */
public class DotNode {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Table table;
    private final DotNodeConfig config;
    private final String path;
    private final String outputDir;
    private final Set<TableColumn> excludedColumns = new HashSet<>();
    private final String lineSeparator = System.getProperty("line.separator");
    private final boolean displayNumRows = Config.getInstance().isNumRowsEnabled();

    /**
     * Create a DotNode that is a focal point of a diagram.
     * That is, all of its columns are displayed in addition to the details
     * of those columns.
     *
     * @param table Table
     * @param path  String
     */
    public DotNode(Table table, String path, String outputDir) {
        this(table, path, outputDir, new DotNodeConfig(true, true));
    }

    public DotNode(Table table, String path, String outputDir, DotNodeConfig config) {
        this.table = table;
        this.path = path + (table.isRemote() ? ("../../" + table.getContainer() + "/tables/") : "");
        this.outputDir = outputDir;
        this.config = config;
    }

    /**
     * Create a DotNode and specify whether it displays its columns.
     * The details of the optional columns (e.g. type, size) are not displayed.
     *
     * @param table       Table
     * @param showColumns boolean
     * @param path        String
     */
    public DotNode(Table table, boolean showColumns, String path, String outputDir) {
        this(table, path, outputDir, showColumns ? new DotNodeConfig(true, false) : new DotNodeConfig());
    }

    public void setShowImplied(boolean showImplied) {
        config.showImpliedRelationships = showImplied;
    }

    public Table getTable() {
        return table;
    }

    public void excludeColumn(TableColumn column) {
        excludedColumns.add(column);
    }

    @Override
    public String toString() {
        StyleSheet css = StyleSheet.getInstance();
        StringBuilder buf = new StringBuilder();
        String tableName = table.getName();
        // fully qualified table name (optionally prefixed with schema)
        String fqTableName = (table.isRemote() ? table.getContainer() + "." : "") + tableName;
        String colspan = config.showColumnDetails ? "COLSPAN=\"2\" " : "COLSPAN=\"3\" ";
        String colspanHeader = config.showColumnDetails ? "COLSPAN=\"4\" " : "COLSPAN=\"3\" ";
        String tableOrView = table.isView() ? "view" : "table";

        buf.append("  \"" + fqTableName + "\" [" + lineSeparator);
        buf.append("   label=<" + lineSeparator);
        buf.append("    <TABLE BORDER=\"" + (config.showColumnDetails ? "2" : "0") + "\" CELLBORDER=\"1\" CELLSPACING=\"0\" BGCOLOR=\"" + css.getTableBackground() + "\">" + lineSeparator);
        buf.append("      <TR>");
        buf.append("<TD " + colspanHeader + " BGCOLOR=\"" + css.getTableHeadBackground() + "\">");
        buf.append("<TABLE BORDER=\"0\" CELLSPACING=\"0\">");
        buf.append("<TR>");
        buf.append("<TD ALIGN=\"LEFT\"><B>" + fqTableName + "</B></TD>");
        buf.append("<TD ALIGN=\"RIGHT\">[" + tableOrView + "]</TD>");
        buf.append("</TR>");
        buf.append("</TABLE>");
        buf.append("</TD>");
        buf.append("</TR>" + lineSeparator);


        boolean skippedTrivial = false;

        if (config.showColumns) {
            List<TableColumn> primaryColumns = table.getPrimaryColumns();
            Set<TableColumn> indexColumns = new HashSet<>();

            for (TableIndex index : table.getIndexes()) {
                indexColumns.addAll(index.getColumns());
            }
            indexColumns.removeAll(primaryColumns);

            int maxwidth = getColumnMaxWidth();

            for (TableColumn column : table.getColumns()) {
                if (config.showTrivialColumns || config.showColumnDetails || column.isPrimary() || column.isForeignKey() || indexColumns.contains(column)) {
                    buf.append("      <TR>");
                    buf.append("<TD PORT=\"" + column.getName() + "\" " + colspan);
                    if (excludedColumns.contains(column))
                        buf.append("BGCOLOR=\"" + css.getExcludedColumnBackgroundColor() + "\" ");
                    else if (indexColumns.contains(column))
                        buf.append("BGCOLOR=\"" + css.getIndexedColumnBackground() + "\" ");
                    buf.append("ALIGN=\"LEFT\">");
                    buf.append("<TABLE BORDER=\"0\" CELLSPACING=\"0\" ALIGN=\"LEFT\">");
                    buf.append("<TR ALIGN=\"LEFT\">");
                    buf.append("<TD ALIGN=\"LEFT\" FIXEDSIZE=\"TRUE\" WIDTH=\"15\" HEIGHT=\"16\">");
                    if (column.isPrimary()) {
                        buf.append("<IMG SRC=\"" + outputDir + "/images/primaryKeys.png\"/>");
                    } else if (column.isForeignKey()) {
                        buf.append("<IMG SRC=\"" + outputDir + "/images/foreignKeys.png\"/>");
                    }
                    buf.append("</TD>");
                    buf.append("<TD ALIGN=\"LEFT\" FIXEDSIZE=\"TRUE\" WIDTH=\"" + maxwidth + "\" HEIGHT=\"16\">");
                    buf.append(column.getName());
                    buf.append("</TD>");
                    buf.append("</TR>");
                    buf.append("</TABLE>");
                    buf.append("</TD>");

                    if (config.showColumnDetails) {
                        buf.append("<TD PORT=\"");
                        buf.append(column.getName());
                        buf.append(".type\" ALIGN=\"LEFT\">");
                        buf.append(column.getShortTypeName().toLowerCase());
                        buf.append("[");
                        buf.append(column.getDetailedSize());
                        buf.append("]</TD>");
                    }
                    buf.append("</TR>" + lineSeparator);
                } else {
                    skippedTrivial = true;
                }
            }
        }

        if (skippedTrivial || !config.showColumns) {
            buf.append("      <TR><TD PORT=\"elipses\" COLSPAN=\"3\" ALIGN=\"LEFT\">...</TD></TR>" + lineSeparator);
        }

        if (!table.isView()) {
            buf.append("      <TR>");
            buf.append("<TD ALIGN=\"LEFT\" BGCOLOR=\"" + css.getBodyBackground() + "\">");
            int numParents = config.showImpliedRelationships ? table.getNumParents() : table.getNumNonImpliedParents();
            if (numParents > 0 || config.showColumnDetails)
                buf.append("&lt; " + numParents);
            else
                buf.append("  ");

            buf.append("</TD>");
            buf.append("<TD ALIGN=\"RIGHT\" BGCOLOR=\"" + css.getBodyBackground() + "\">");
            final long numRows = table.getNumRows();
            if (displayNumRows && numRows >= 0) {
                buf.append(NumberFormat.getInstance().format(numRows));
                buf.append(" row");
                if (numRows != 1)
                    buf.append('s');
            } else {
                buf.append("  ");
            }
            buf.append("</TD>");

            buf.append("<TD ALIGN=\"RIGHT\" BGCOLOR=\"" + css.getBodyBackground() + "\">");
            int numChildren = config.showImpliedRelationships ? table.getNumChildren() : table.getNumNonImpliedChildren();
            if (numChildren > 0 || config.showColumnDetails)
                buf.append(numChildren + " &gt;");
            else
                buf.append("  ");
            buf.append("</TD></TR>" + lineSeparator);
        }

        buf.append("    </TABLE>>" + lineSeparator);
        if (!table.isRemote() || Config.getInstance().isOneOfMultipleSchemas())
            buf.append("    URL=\"" + path + urlEncodeLink(tableName) + ".html\"" + lineSeparator);
        buf.append("    tooltip=\"" + escapeHtml(fqTableName) + "\"" + lineSeparator);
        buf.append("  ];");

        return buf.toString();
    }

    private int getColumnMaxWidth() {
        int maxWidth = getTextWidth(table.getName());
        for (TableColumn column : table.getColumns()) {
            int size = getTextWidth(column.getName());
            if (maxWidth < size) {
                maxWidth = size;
            }
        }
        return maxWidth;
    }

    private static int getTextWidth(String text) {
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
        int fontSize = Config.getInstance().getFontSize() + 1;
        Font font = new Font(Config.getInstance().getFont(), Font.BOLD, fontSize);
        return (int) (font.getStringBounds(text, frc).getWidth());
    }

    /**
     * HTML escape the specified string
     *
     * @param string
     * @return
     */
    private static String escapeHtml(String string) {
        StringWriter writer = new StringWriter();
        //TODO Try to replace usage of HtmlEscaper so that the "dot-lang" doesn't have dependency on mustache
        HtmlEscaper.escape(string, writer);
        return writer.toString();
    }

    private static String urlEncodeLink(String string) {
        try {
            return URLEncoder.encode(string, StandardCharsets.UTF_8.name()).replace("+","%20");
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Error trying to urlEncode string [{}] with encoding [{}]", string, StandardCharsets.UTF_8.name(), e);
            return string;
        }
    }

    public static class DotNodeConfig {
        private final boolean showColumns;
        private boolean showTrivialColumns;
        private final boolean showColumnDetails;
        private boolean showImpliedRelationships;

        /**
         * Nothing but table name and counts are displayed
         */
        public DotNodeConfig() {
            showColumns = showTrivialColumns = showColumnDetails = showImpliedRelationships = false;
        }

        public DotNodeConfig(boolean showTrivialColumns, boolean showColumnDetails) {
            showColumns = true;
            this.showTrivialColumns = showTrivialColumns;
            this.showColumnDetails = showColumnDetails;
        }
    }
}