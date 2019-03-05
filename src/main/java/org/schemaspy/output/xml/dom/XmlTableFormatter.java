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

import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static org.schemaspy.output.xml.dom.XmlConstants.*;

/**
 * Formats {@link Table}s into an XML DOM tree.
 *
 * @author John Currier
 * @author Daniel Watt
 * @author Nils Petzaell
 */
public class XmlTableFormatter {

    private final XmlColumnFormatter xmlColumnFormatter = new XmlColumnFormatter();
    private final XmlIndexFormatter xmlIndexFormatter = new XmlIndexFormatter();

    /**
     * Append the specified tables to the XML node
     *
     * @param schemaNode
     * @param tables
     */
    public void appendTables(Element schemaNode, Collection<Table> tables) {
        Document document = schemaNode.getOwnerDocument();
        Element tablesNode = document.createElement("tables");
        schemaNode.appendChild(tablesNode);
        tables.stream()
                .sorted((table1, table2) -> table1.getName().compareToIgnoreCase(table2.getName()))
                .distinct()
                .forEachOrdered( t -> appendTable(tablesNode, t));
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
        xmlColumnFormatter.appendColumns(tableNode, table);
        appendPrimaryKeys(tableNode, table);
        xmlIndexFormatter.appendIndexes(tableNode, table);
        appendCheckConstraints(tableNode, table);
        appendView(tableNode, table);
    }

    /**
     * Append primary key details to the XML node
     *
     * @param tableNode
     * @param table
     */
    private static void appendPrimaryKeys(Element tableNode, Table table) {
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
    private static void appendCheckConstraints(Element tableNode, Table table) {
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
     * Append view SQL to the XML node
     *
     * @param tableNode
     * @param table
     */
    private static void appendView(Element tableNode, Table table) {
        if (table.isView() && Objects.nonNull(table.getViewDefinition())) {
            DOMUtil.appendAttribute(tableNode, "viewSql", table.getViewDefinition());
        }
    }
}