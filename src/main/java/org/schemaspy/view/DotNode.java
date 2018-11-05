/*
 * Copyright (C) 2004 - 2011 John Currier
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2017 Wojciech Kasa
 * Copyright (C) 2017 Daniel Watt
 * Copyright (C) 2018 Nils Petzaell
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
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.schemaspy.model.TableIndex;
import org.schemaspy.util.Html;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
    private static final Html html = new Html();
    private static final StyleSheet CSS = StyleSheet.getInstance();

    private static final String INDENT_6 = "      ";

    private final Table table;
    private final DotNodeConfig config;
    private final String path;
    private final String outputDir;
    private final Set<TableColumn> excludedColumns = new HashSet<>();
    private final String lineSeparator = System.getProperty("line.separator");
    private final boolean displayNumRows = Config.getInstance().isNumRowsEnabled();
    private final String columnSpan;

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
        this.columnSpan = config.showColumnDetails ? "COLSPAN=\"2\" " : "COLSPAN=\"3\" ";
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
        StringBuilder buf = new StringBuilder();
        String tableName = table.getName();
        // fully qualified table name (optionally prefixed with schema)
        String fqTableName = (table.isRemote() ? table.getContainer() + "." : "") + tableName;
        String colspanHeader = config.showColumnDetails ? "COLSPAN=\"4\" " : "COLSPAN=\"3\" ";
        String tableOrView = table.isView() ? "view" : "table";

        buf.append("  \"" + fqTableName + "\" [" + lineSeparator);
        buf.append("   label=<" + lineSeparator);
        buf.append("    <TABLE BORDER=\"" + (config.showColumnDetails ? "2" : "0") + "\" CELLBORDER=\"1\" CELLSPACING=\"0\" BGCOLOR=\"" + CSS.getTableBackground() + "\">" + lineSeparator);
        buf.append(INDENT_6 + Html.TR_START);
        buf.append("<TD " + colspanHeader + " BGCOLOR=\"" + CSS.getTableHeadBackground() + "\">");
        buf.append("<TABLE BORDER=\"0\" CELLSPACING=\"0\">");
        buf.append(Html.TR_START);
        buf.append("<TD ALIGN=\"LEFT\"><B>" + escapeHtml(fqTableName) + "</B>" + Html.TD_END);
        buf.append("<TD ALIGN=\"RIGHT\">[" + tableOrView + "]" + Html.TD_END);
        buf.append(Html.TR_END);
        buf.append("</TABLE>");
        buf.append(Html.TD_END);
        buf.append(Html.TR_END + lineSeparator);

        buf.append(columnsToString());
        if (!table.isView()) {
            buf.append(tableToString());
        }

        buf.append("    </TABLE>>" + lineSeparator);
        if (!table.isRemote() || Config.getInstance().isOneOfMultipleSchemas())
            buf.append("    URL=\"" + path + urlEncodeLink(tableName) + ".html\"" + lineSeparator);
        buf.append("    tooltip=\"" + escapeHtml(fqTableName) + "\"" + lineSeparator);
        buf.append("  ];");

        return buf.toString();
    }

    private String columnsToString() {
        StringBuilder buf = new StringBuilder();
        boolean skippedTrivial = false;

        if (config.showColumns) {
            Set<TableColumn> indexColumns = getIndexColumns();
            int maxWidth = getColumnMaxWidth();
            for (TableColumn column : table.getColumns()) {
                if (config.showTrivialColumns || config.showColumnDetails || column.isPrimary() || column.isForeignKey() || indexColumns.contains(column)) {
                    buf.append(columnToString(column, indexColumns, maxWidth));
                } else {
                    skippedTrivial = true;
                }
            }
        }

        if (skippedTrivial || !config.showColumns) {
            buf.append(INDENT_6 + Html.TR_START + "<TD PORT=\"elipses\" COLSPAN=\"3\" ALIGN=\"LEFT\">..." + Html.TD_END + Html.TR_END + lineSeparator);
        }
        return buf.toString();
    }

    private Set<TableColumn> getIndexColumns() {
        Set<TableColumn> indexColumns = new LinkedHashSet<>();
        for (TableIndex index : table.getIndexes()) {
            indexColumns.addAll(index.getColumns());
        }
        indexColumns.removeAll(table.getPrimaryColumns());
        return indexColumns;
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

    private String columnToString(TableColumn column, Set<TableColumn> indexColumns, int maxWidth) {
        StringBuilder buf = new StringBuilder();
        buf.append(INDENT_6 + Html.TR_START);
        buf.append("<TD PORT=\"" + escapeHtml(column.getName()) + "\" " + columnSpan);
        if (excludedColumns.contains(column))
            buf.append("BGCOLOR=\"" + CSS.getExcludedColumnBackgroundColor() + "\" ");
        else if (indexColumns.contains(column))
            buf.append("BGCOLOR=\"" + CSS.getIndexedColumnBackground() + "\" ");
        buf.append("ALIGN=\"LEFT\">");
        buf.append("<TABLE BORDER=\"0\" CELLSPACING=\"0\" ALIGN=\"LEFT\">");
        buf.append("<TR ALIGN=\"LEFT\">");
        buf.append("<TD ALIGN=\"LEFT\" FIXEDSIZE=\"TRUE\" WIDTH=\"15\" HEIGHT=\"16\">");
        if (column.isPrimary()) {
            buf.append("<IMG SRC=\"" + outputDir + "/images/primaryKeys.png\"/>");
        } else if (column.isForeignKey()) {
            buf.append("<IMG SRC=\"" + outputDir + "/images/foreignKeys.png\"/>");
        }
        buf.append(Html.TD_END);
        buf.append("<TD ALIGN=\"LEFT\" FIXEDSIZE=\"TRUE\" WIDTH=\"" + maxWidth + "\" HEIGHT=\"16\">");
        buf.append(escapeHtml(column.getName()));
        buf.append(Html.TD_END);
        buf.append(Html.TR_END);
        buf.append("</TABLE>");
        buf.append(Html.TD_END);

        if (config.showColumnDetails) {
            buf.append("<TD PORT=\"");
            buf.append(escapeHtml(column.getName()));
            buf.append(".type\" ALIGN=\"LEFT\">");
            buf.append(column.getShortTypeName().toLowerCase());
            buf.append("[");
            buf.append(column.getDetailedSize());
            buf.append("]" + Html.TD_END);
        }
        buf.append(Html.TR_END + lineSeparator);
        return buf.toString();
    }

    private String tableToString() {
        StringBuilder buf = new StringBuilder();
        buf.append(INDENT_6 + Html.TR_START);
        buf.append("<TD ALIGN=\"LEFT\" BGCOLOR=\"" + CSS.getBodyBackground() + "\">");
        int numParents = config.showImpliedRelationships ? table.getNumParents() : table.getNumNonImpliedParents();
        if (numParents > 0 || config.showColumnDetails)
            buf.append("&lt; " + numParents);
        else
            buf.append("  ");

        buf.append(Html.TD_END);
        buf.append("<TD ALIGN=\"RIGHT\" BGCOLOR=\"" + CSS.getBodyBackground() + "\">");
        final long numRows = table.getNumRows();
        if (displayNumRows && numRows >= 0) {
            buf.append(NumberFormat.getInstance().format(numRows));
            buf.append(" row");
            if (numRows != 1)
                buf.append('s');
        } else {
            buf.append("  ");
        }
        buf.append(Html.TD_END);

        buf.append("<TD ALIGN=\"RIGHT\" BGCOLOR=\"" + CSS.getBodyBackground() + "\">");
        int numChildren = config.showImpliedRelationships ? table.getNumChildren() : table.getNumNonImpliedChildren();
        if (numChildren > 0 || config.showColumnDetails)
            buf.append(numChildren + " &gt;");
        else
            buf.append("  ");
        buf.append(Html.TD_END + Html.TR_END + lineSeparator);
        return buf.toString();
    }

    /**
     * HTML escape the specified string
     *
     * @param string
     * @return
     */
    private static String escapeHtml(String string) {
        return html.escape(string);
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