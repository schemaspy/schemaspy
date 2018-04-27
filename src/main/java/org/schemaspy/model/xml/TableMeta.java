/*
 * Copyright (C) 2004 - 2011 John Currier
 * Copyright (C) 2017 Daniel Watt
 * Copyright (C) 2017 Nils Petzaell
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
package org.schemaspy.model.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Additional metadata about a table as expressed in XML instead of from
 * the database.
 *
 * @author John Currier
 * @author Daniel Watt
 * @author Nils Petzaell
 */
public class TableMeta {
    private final String name;
    private final String comments;
    private final List<TableColumnMeta> columns = new ArrayList<TableColumnMeta>();
    private final String remoteCatalog;
    private final String remoteSchema;

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    TableMeta(Node tableNode) {
        NamedNodeMap attribs = tableNode.getAttributes();

        name = attribs.getNamedItem("name").getNodeValue();

        Node node = attribs.getNamedItem("comments");
        if (node == null) {
            node = attribs.getNamedItem("remarks");
            if (Objects.nonNull(node)) {
                LOGGER.warn("<remarks> has been deprecated");
            }
        }
        if (node != null) {
            String tmp = node.getNodeValue().trim();
            comments = tmp.length() == 0 ? null : tmp;
        } else {
            comments = null;
        }

        node = attribs.getNamedItem("remoteSchema");
        remoteSchema = node == null ? null : node.getNodeValue().trim();

        node = attribs.getNamedItem("remoteCatalog");
        remoteCatalog = node == null ? null : node.getNodeValue().trim();

        LOGGER.debug("Found XML table metadata for {} remoteCatalog: {} remoteSchema: {} comments: {}", name, remoteCatalog, remoteSchema, comments);

        NodeList columnNodes = ((Element)tableNode.getChildNodes()).getElementsByTagName("column");

        for (int i = 0; i < columnNodes.getLength(); ++i) {
            Node colNode = columnNodes.item(i);
            columns.add(new TableColumnMeta(colNode));
        }
    }

    public String getName() {
        return name;
    }

    public String getComments() {
        return comments;
    }

    public List<TableColumnMeta> getColumns() {
        return columns;
    }

    public String getRemoteCatalog() {
        return remoteCatalog;
    }
    
    public String getRemoteSchema() {
        return remoteSchema;
    }
}