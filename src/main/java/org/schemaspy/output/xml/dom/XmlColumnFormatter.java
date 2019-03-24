/*
 * Copyright (C) 2004 - 2011 John Currier
 * Copyright (C) 2017 Daniel Watt
 * Copyright (C) 2017 - 2019 Nils Petzaell
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.nio.charset.StandardCharsets;
import java.sql.Types;
import java.util.regex.Pattern;

import static org.schemaspy.output.xml.dom.XmlConstants.*;

/**
 * Formats {@link TableColumn}s into an XML DOM tree.
 *
 * @author John Currier
 * @author Nils Petzaell
 */
public class XmlColumnFormatter {
    private static final int DEFAULT_JDBC_TYPE_CODE = Types.VARCHAR;

    // valid chars came from http://www.w3.org/TR/REC-xml/#charsets
    // and attempting to match 0x10000-0x10FFFF with the \p Unicode escapes
    // (from http://www.regular-expressions.info/unicode.html)
    private static final Pattern validXmlChars =
            Pattern.compile("^[ -\uD7FF\uE000-\uFFFD\\p{L}\\p{M}\\p{Z}\\p{S}\\p{N}\\p{P}]*$");

    /**
     * Append all columns in the table to the XML node
     *
     * @param tableNode
     * @param table
     */
    public void appendColumns(Element tableNode, Table table) {
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
    public Node appendColumn(Node tableNode, TableColumn column) {
        Document document = tableNode.getOwnerDocument();
        Node columnNode = document.createElement(COLUMN);
        tableNode.appendChild(columnNode);

        DOMUtil.appendAttribute(columnNode, "id", String.valueOf(column.getId()));
        DOMUtil.appendAttribute(columnNode, "name", column.getName());
        DOMUtil.appendAttribute(columnNode, "type", column.getTypeName());
        DOMUtil.appendAttribute(columnNode, "typeCode", String.valueOf(column.getType() == null ?
                DEFAULT_JDBC_TYPE_CODE : column.getType()));
        DOMUtil.appendAttribute(columnNode, "defaultValue", String.valueOf(column.getDefaultValue()));
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
            columnNode.appendChild(childNode);
            appendForeignKeyAttributes(childNode, childColumn, column.getChildConstraint(childColumn));
        }

        for (TableColumn parentColumn : column.getParents()) {
            Node parentNode = document.createElement("parent");
            columnNode.appendChild(parentNode);
            appendForeignKeyAttributes(parentNode, parentColumn, column.getParentConstraint(parentColumn));
        }

        return columnNode;
    }

    private static void appendForeignKeyAttributes(Node node, TableColumn column, ForeignKeyConstraint foreignKeyConstraint) {
        Table table = column.getTable();
        DOMUtil.appendAttribute(node, "foreignKey", foreignKeyConstraint.getName());
        DOMUtil.appendAttribute(node, CATALOG, table.getCatalog());
        DOMUtil.appendAttribute(node, SCHEMA, table.getSchema());
        DOMUtil.appendAttribute(node, TABLE, table.getName());
        DOMUtil.appendAttribute(node, COLUMN, column.getName());
        DOMUtil.appendAttribute(node, "implied", String.valueOf(foreignKeyConstraint.isImplied()));
        DOMUtil.appendAttribute(node, "onDeleteCascade", String.valueOf(foreignKeyConstraint.isCascadeOnDelete()));
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
    private static String asBinary(String str) {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        StringBuilder buf = new StringBuilder(bytes.length * 2);
        for (byte aByte : bytes) {
            buf.append(String.format("%02X", aByte));
        }
        return buf.toString();
    }
}
