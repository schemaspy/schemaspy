/*
 * Copyright (C) 2004 - 2011 John Currier
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2017 Wojciech Kasa
 * Copyright (C) 2017 Daniel Watt
 * Copyright (C) 2018 Nils Petzaell
 * Copyright (C) 2019 Kamyab Nazari
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

import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.schemaspy.model.TableIndex;
import org.schemaspy.output.dot.RuntimeDotConfig;
import org.schemaspy.output.dot.schemaspy.node.Footer;
import org.schemaspy.output.dot.schemaspy.node.footer.Children;
import org.schemaspy.output.dot.schemaspy.node.footer.Parents;
import org.schemaspy.output.dot.schemaspy.node.footer.Rows;
import org.schemaspy.util.naming.FileNameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author John Currier
 * @author Rafal Kasa
 * @author Wojciech Kasa
 * @author Thomas Traude
 * @author Daniel Watt
 * @author Nils Petzaell
 */
public class DotNode implements Node {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final Html html = new Html();
    private static final String INDENT_6 = "      ";
    private static final String TABLES_PATH = "/tables/"; //NOSONAR

    private final Table table;
    private final String path;
    private final DotNodeConfig config;
    private final RuntimeDotConfig runtimeDotConfig;
    private final String lineSeparator = System.getProperty("line.separator");
    private final String columnSpan;

    private boolean showImpliedRelationships;

    public DotNode(Table table, boolean fromRoot, DotNodeConfig config, RuntimeDotConfig runtimeDotConfig) {
        this.table = table;
        this.config = config;
        this.runtimeDotConfig = runtimeDotConfig;
        this.path = createPath(fromRoot);
        this.columnSpan = config.showColumnDetails ? "COLSPAN=\"2\" " : "COLSPAN=\"3\" ";
    }

    private String createPath(boolean fromRoot) {
        if (runtimeDotConfig.useRelativeLinks()) {
            return (table.isRemote() ? "../../../" + new FileNameGenerator(table.getContainer()).value() : "../..") + TABLES_PATH;
        }
        if (fromRoot) {
            return (table.isRemote() ? ("../" + new FileNameGenerator(table.getContainer()).value() + TABLES_PATH) : "tables/");
        }
        return (table.isRemote() ? ("../../" + new FileNameGenerator(table.getContainer()).value() + TABLES_PATH) : "");

    }

    public void setShowImplied(boolean showImplied) {
        showImpliedRelationships = showImplied;
    }

    public Table getTable() {
        return table;
    }

    @Override
    public String value() {
        return toString();
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        String tableName = table.getName();
        // fully qualified table name (optionally prefixed with schema)
        String fqTableName = (table.isRemote() ? table.getContainer() + "." : "") + tableName;
        int maxTitleWidth = getTitleMaxWidth(fqTableName);
        String colspanHeader = config.showColumnDetails ? "COLSPAN=\"4\" " : "COLSPAN=\"3\" ";
        String tableOrView = table.isView() ? "view" : "table";

        buf.append("  \"" + fqTableName + "\" [" + lineSeparator);
        buf.append("   label=<" + lineSeparator);
        buf.append("    <TABLE BORDER=\"" + (config.showColumnDetails ? "2" : "0") + "\" CELLBORDER=\"1\" CELLSPACING=\"0\" BGCOLOR=\"" + runtimeDotConfig.styleSheet().getTableBackground() + "\">" + lineSeparator);
        buf.append(INDENT_6 + Html.TR_START);
        buf.append("<TD " + colspanHeader + " BGCOLOR=\"" + runtimeDotConfig.styleSheet().getTableHeadBackground() + "\">");
        buf.append("<TABLE BORDER=\"0\" CELLSPACING=\"0\">");
        buf.append(Html.TR_START);
        buf.append("<TD ALIGN=\"LEFT\" FIXEDSIZE=\"TRUE\" WIDTH=\"" + maxTitleWidth + "\" HEIGHT=\"16\"><B>" + escapeHtml(fqTableName) +"</B>" + Html.TD_END);
        buf.append("<TD ALIGN=\"RIGHT\">[" + tableOrView + "]" + Html.TD_END);
        buf.append(Html.TR_END);
        buf.append("</TABLE>");
        buf.append(Html.TD_END);
        buf.append(Html.TR_END + lineSeparator);

        buf.append(columnsToString());
        if (!table.isView()) {
            buf.append(footerToString());
        }

        buf.append("    </TABLE>>" + lineSeparator);
        if (!table.isRemote() || runtimeDotConfig.isOneOfMultipleSchemas()) {
            buf.append("    URL=\"" + path + urlEncodeLink(new FileNameGenerator(tableName).value()) + ".html\"" + lineSeparator);
            buf.append("    target=\"_top\"" + lineSeparator);
        }
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

    private int getTitleMaxWidth(String titleTable) {
        return getTextWidth(titleTable);
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

    private int getTextWidth(String text) {
        return runtimeDotConfig.getTextWidth(text);
    }

    private String columnToString(TableColumn column, Set<TableColumn> indexColumns, int maxWidth) {
        StringBuilder buf = new StringBuilder();
        buf.append(INDENT_6 + Html.TR_START);
        buf.append("<TD PORT=\"" + escapeHtml(column.getName()) + "\" " + columnSpan);
        if (column.isExcluded())
            buf.append("BGCOLOR=\"" + runtimeDotConfig.styleSheet().getExcludedColumnBackgroundColor() + "\" ");
        else if (indexColumns.contains(column))
            buf.append("BGCOLOR=\"" + runtimeDotConfig.styleSheet().getIndexedColumnBackground() + "\" ");
        buf.append("ALIGN=\"LEFT\">");
        buf.append("<TABLE BORDER=\"0\" CELLSPACING=\"0\" ALIGN=\"LEFT\">");
        buf.append("<TR ALIGN=\"LEFT\">");
        buf.append("<TD ALIGN=\"LEFT\" FIXEDSIZE=\"TRUE\" WIDTH=\"15\" HEIGHT=\"16\">");
        if (column.isPrimary()) {
            buf.append("<IMG SRC=\"../../images/primaryKeys.png\"/>");
        } else if (column.isForeignKey()) {
            buf.append("<IMG SRC=\"../../images/foreignKeys.png\"/>");
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
            buf.append(escapeHtml(column.getShortTypeName().toLowerCase()));
            if (Objects.nonNull(column.getDetailedSize()) && !column.getDetailedSize().isEmpty()) {
                buf.append("[");
                buf.append(escapeHtml(column.getDetailedSize()));
                buf.append("]");
            }
            buf.append(Html.TD_END);
        }
        buf.append(Html.TR_END + lineSeparator);
        return buf.toString();
    }

    private String footerToString() {
        String colspan = config.showColumnDetails ? "COLSPAN=\"4\" " : "COLSPAN=\"3\" ";
        StringBuilder buf = new StringBuilder();
        buf.append(INDENT_6 + Html.TR_START);
        buf.append("<TD ALIGN=\"LEFT\" CELLPADDING=\"0\" BGCOLOR=\"" + runtimeDotConfig.styleSheet().getBodyBackground() + "\" " + colspan + ">");
        buf.append(
            new Footer(
                runtimeDotConfig.styleSheet().getBodyBackground(),
                new Parents(
                    showImpliedRelationships ? table.getNumParents() : table.getNumNonImpliedParents(),
                    config.showColumnDetails
                ),
                new Rows(
                    table.getNumRows(),
                    runtimeDotConfig.isNumRowsEnabled()
                ),
                new Children(
                    showImpliedRelationships ? table.getNumChildren() : table.getNumNonImpliedChildren(),
                    config.showColumnDetails
                )
            ).value()
        );

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

}
