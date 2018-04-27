/*
 * Copyright (C) 2004 - 2011 John Currier
 * Copyright (C) 2017 Daniel Watt
 * Copyright (C) 2017 - 2018 Nils Petzaell
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
package org.schemaspy.output.xml.dom;

import org.schemaspy.model.ForeignKeyConstraint;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.schemaspy.model.TableIndex;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * Formats {@link Table}s into an XML DOM tree.
 *
 * @author John Currier
 * @author Daniel Watt
 * @author Nils Petzaell
 */
public class XmlTableFormatter {

    private static final String CATALOG = "catalog";
    private static final String SCHEMA = "schema";
    private static final String TABLE = "table";
    private static final String COLUMN = "column";

    private static final XmlTableFormatter instance = new XmlTableFormatter();

    // valid chars came from http://www.w3.org/TR/REC-xml/#charsets
    // and attempting to match 0x10000-0x10FFFF with the \p Unicode escapes
    // (from http://www.regular-expressions.info/unicode.html)
    private static final Pattern validXmlChars =
        Pattern.compile("^[ -\uD7FF\uE000-\uFFFD\\p{L}\\p{M}\\p{Z}\\p{S}\\p{N}\\p{P}]*$");

    /**
     * Singleton...don't allow instantiation
     */
    private XmlTableFormatter() {}

    /**
     * Singleton accessor
     *
     * @return
     */
    public static XmlTableFormatter getInstance() {
        return instance;
    }

    /**
     * Append the specified tables to the XML node
     *
     * @param schemaNode
     * @param tables
     */
    public void appendTables(Element schemaNode, Collection<Table> tables) {
        Set<Table> byName = new TreeSet<>((table1, table2) ->
                table1.getName().compareToIgnoreCase(table2.getName())
        );
        byName.addAll(tables);

        Document document = schemaNode.getOwnerDocument();
        Element tablesNode = document.createElement("tables");
        schemaNode.appendChild(tablesNode);
        for (Table table : byName)
            appendTable(tablesNode, table);
    }

    /**
     * Append table details to the XML node
     *
     * @param tablesNode
     * @param table
     */
    private void appendTable(Element tablesNode, Table table) {
        Document document = tablesNode.getOwnerDocument();
        Element tableNode = document.createElement(TABLE);
        tablesNode.appendChild(tableNode);
        if (table.getId() != null)
            DOMUtil.appendAttribute(tableNode, "id", String.valueOf(table.getId()));
        DOMUtil.appendAttribute(tableNode, CATALOG, table.getCatalog());
        DOMUtil.appendAttribute(tableNode, SCHEMA, table.getSchema());
        DOMUtil.appendAttribute(tableNode, "name", table.getName());
        if (table.getNumRows() >= 0)
            DOMUtil.appendAttribute(tableNode, "numRows", String.valueOf(table.getNumRows()));
        DOMUtil.appendAttribute(tableNode, "type", table.isView() ? "VIEW" : "TABLE");
        DOMUtil.appendAttribute(tableNode, "remarks", table.getComments() == null ? "" : table.getComments());
        appendColumns(tableNode, table);
        appendPrimaryKeys(tableNode, table);
        appendIndexes(tableNode, table);
        appendCheckConstraints(tableNode, table);
        appendView(tableNode, table);
    }

    /**
     * Append all columns in the table to the XML node
     *
     * @param tableNode
     * @param table
     */
    private void appendColumns(Element tableNode, Table table) {
        for (TableColumn column : table.getColumns()) {
            appendColumn(tableNode, column);
        }
    }

    /**
     * Append column details to the XML node
     *
     * @param tableNode
     * @param column
     * @return
     */
    private Node appendColumn(Node tableNode, TableColumn column) {
        Document document = tableNode.getOwnerDocument();
        Node columnNode = document.createElement(COLUMN);
        tableNode.appendChild(columnNode);

        DOMUtil.appendAttribute(columnNode, "id", String.valueOf(column.getId()));
        DOMUtil.appendAttribute(columnNode, "name", column.getName());
        DOMUtil.appendAttribute(columnNode, "type", column.getTypeName());
        DOMUtil.appendAttribute(columnNode, "size", String.valueOf(column.getLength()));
        DOMUtil.appendAttribute(columnNode, "digits", String.valueOf(column.getDecimalDigits()));
        DOMUtil.appendAttribute(columnNode, "nullable", String.valueOf(column.isNullable()));
        DOMUtil.appendAttribute(columnNode, "autoUpdated", String.valueOf(column.isAutoUpdated()));
        if (column.getDefaultValue() != null) {
            String defaultValue = column.getDefaultValue().toString();
            if (isBinary(defaultValue)) {
                // we're run into a binary default value, convert it to its hex equivalent
                defaultValue = asBinary(defaultValue);
                // and indicate that it's been converted
                DOMUtil.appendAttribute(columnNode, "defaultValueIsBinary", "true");
            }
            DOMUtil.appendAttribute(columnNode, "defaultValue", defaultValue);
        }
        DOMUtil.appendAttribute(columnNode, "remarks", column.getComments() == null ? "" : column.getComments());

        for (TableColumn childColumn : column.getChildren()) {
            Node childNode = document.createElement("child");
            Table table = childColumn.getTable();
            columnNode.appendChild(childNode);
            ForeignKeyConstraint constraint = column.getChildConstraint(childColumn);
            DOMUtil.appendAttribute(childNode, "foreignKey", constraint.getName());
            DOMUtil.appendAttribute(childNode, CATALOG, table.getCatalog());
            DOMUtil.appendAttribute(childNode, SCHEMA, table.getSchema());
            DOMUtil.appendAttribute(childNode, TABLE, table.getName());
            DOMUtil.appendAttribute(childNode, COLUMN, childColumn.getName());
            DOMUtil.appendAttribute(childNode, "implied", String.valueOf(constraint.isImplied()));
            DOMUtil.appendAttribute(childNode, "onDeleteCascade", String.valueOf(constraint.isCascadeOnDelete()));
        }

        for (TableColumn parentColumn : column.getParents()) {
            Node parentNode = document.createElement("parent");
            Table table = parentColumn.getTable();
            columnNode.appendChild(parentNode);
            ForeignKeyConstraint constraint = column.getParentConstraint(parentColumn);
            DOMUtil.appendAttribute(parentNode, "foreignKey", constraint.getName());
            DOMUtil.appendAttribute(parentNode, CATALOG, table.getCatalog());
            DOMUtil.appendAttribute(parentNode, SCHEMA, table.getSchema());
            DOMUtil.appendAttribute(parentNode, TABLE, table.getName());
            DOMUtil.appendAttribute(parentNode, COLUMN, parentColumn.getName());
            DOMUtil.appendAttribute(parentNode, "implied", String.valueOf(constraint.isImplied()));
            DOMUtil.appendAttribute(parentNode, "onDeleteCascade", String.valueOf(constraint.isCascadeOnDelete()));
        }

        return columnNode;
    }

    /**
     * Append primary key details to the XML node
     *
     * @param tableNode
     * @param table
     */
    private void appendPrimaryKeys(Element tableNode, Table table) {
        Document document = tableNode.getOwnerDocument();
        int index = 1;

        for (TableColumn primaryKeyColumn : table.getPrimaryColumns()) {
            Node primaryKeyNode = document.createElement("primaryKey");
            tableNode.appendChild(primaryKeyNode);

            DOMUtil.appendAttribute(primaryKeyNode, COLUMN, primaryKeyColumn.getName());
            DOMUtil.appendAttribute(primaryKeyNode, "sequenceNumberInPK", String.valueOf(index++));
        }
    }

    /**
     * Append check constraint details to the XML node
     *
     * @param tableNode
     * @param table
     */
    private void appendCheckConstraints(Element tableNode, Table table) {
        Document document = tableNode.getOwnerDocument();
        Map<String, String> constraints = table.getCheckConstraints();
        if (constraints != null && !constraints.isEmpty()) {
            constraints.forEach((name, value) -> {
                Node constraintNode = document.createElement("checkConstraint");
                tableNode.appendChild(constraintNode);

                DOMUtil.appendAttribute(constraintNode, "name", name);
                DOMUtil.appendAttribute(constraintNode, "constraint", value);
            });
        }
    }

    /**
     * Append index details to the XML node
     *
     * @param tableNode
     * @param table
     */
    private void appendIndexes(Node tableNode, Table table) {
        boolean showId = table.getId() != null;
        Set<TableIndex> indexes = table.getIndexes();
        if (indexes != null && !indexes.isEmpty()) {
            indexes = new TreeSet<>(indexes); // sort primary keys first
            Document document = tableNode.getOwnerDocument();

            for (TableIndex index : indexes) {
                Node indexNode = document.createElement("index");

                if (showId)
                    DOMUtil.appendAttribute(indexNode, "id", String.valueOf(index.getId()));
                DOMUtil.appendAttribute(indexNode, "name", index.getName());
                DOMUtil.appendAttribute(indexNode, "unique", String.valueOf(index.isUnique()));

                for (TableColumn column : index.getColumns()) {
                    Node columnNode = document.createElement(COLUMN);

                    DOMUtil.appendAttribute(columnNode, "name", column.getName());
                    DOMUtil.appendAttribute(columnNode, "ascending", String.valueOf(index.isAscending(column)));
                    indexNode.appendChild(columnNode);
                }
                tableNode.appendChild(indexNode);
            }
        }
    }

    /**
     * Append view SQL to the XML node
     *
     * @param tableNode
     * @param table
     */
    private void appendView(Element tableNode, Table table) {
        String sql;
        if (table.isView() && (sql = table.getViewDefinition()) != null) {
            DOMUtil.appendAttribute(tableNode, "viewSql", sql);
        }
    }

    /**
     * Returns <code>true</code> if the string contains binary data
     * (chars that are invalid for XML) per http://www.w3.org/TR/REC-xml/#charsets
     *
     * @param str
     * @return
     */
    private static boolean isBinary(String str) {
        return !validXmlChars.matcher(str).matches();
    }

    /**
     * Turns a string into its hex equivalent.
     * Intended to be used when {@link #isBinary(String)} returns <code>true</code>.
     *
     * @param str
     * @return
     */
    private String asBinary(String str) {
        byte[] bytes = str.getBytes();
        StringBuilder buf = new StringBuilder(bytes.length * 2);
		for (byte aByte : bytes) {
			buf.append(String.format("%02X", aByte));
		}
        return buf.toString();
    }
}