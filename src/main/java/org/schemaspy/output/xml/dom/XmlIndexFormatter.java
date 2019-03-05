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

import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.schemaspy.model.TableIndex;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import static org.schemaspy.output.xml.dom.XmlConstants.COLUMN;

/**
 * Formats {@link TableIndex}s into an XML DOM tree.
 *
 * @author John Currier
 * @author Daniel Watt
 * @author Nils Petzaell
 */
public class XmlIndexFormatter {

    /**
     * Append index details to the XML node
     *
     * @param tableNode
     * @param table
     */
    public void appendIndexes(Node tableNode, Table table) {
        boolean showId = table.getId() != null;
        table.getIndexes().stream().sorted().forEachOrdered(
                i -> appendIndex(tableNode, i, showId)
        );
    }

    /**
     * Append index details to the XML node
     *
     * @param tableNode
     * @param tableIndex
     * @param showId
     */

    public void appendIndex(Node tableNode, TableIndex tableIndex, boolean showId) {
        Document document = tableNode.getOwnerDocument();
        Node indexNode = document.createElement("index");

        if (showId) {
            DOMUtil.appendAttribute(indexNode, "id", String.valueOf(tableIndex.getId()));
        }
        DOMUtil.appendAttribute(indexNode, "name", tableIndex.getName());
        DOMUtil.appendAttribute(indexNode, "unique", String.valueOf(tableIndex.isUnique()));

        for (TableColumn column : tableIndex.getColumns()) {
            Node columnNode = document.createElement(COLUMN);

            DOMUtil.appendAttribute(columnNode, "name", column.getName());
            DOMUtil.appendAttribute(columnNode, "ascending", String.valueOf(tableIndex.isAscending(column)));
            indexNode.appendChild(columnNode);
        }
        tableNode.appendChild(indexNode);
    }

}
