/*
 * Copyright (C) 2004 - 2011 John Currier
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
package org.schemaspy.model.xml;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.lang.invoke.MethodHandles;

/**
 * Additional metadata about a foreign key relationship as expressed in XML
 * instead of from the database.
 *
 * @author John Currier
 * @author Daniel Watt
 */
public class ForeignKeyMeta {
    private final String tableName;
    private final String columnName;
    private final String remoteCatalog;
    private final String remoteSchema;
    private final static Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public ForeignKeyMeta(Node foreignKeyNode) {
        NamedNodeMap attribs = foreignKeyNode.getAttributes();
        Node node = attribs.getNamedItem("table");
        if (node == null)
            throw new IllegalStateException("XML foreignKey definition requires 'table' attribute");
        tableName = node.getNodeValue();
        node = attribs.getNamedItem("column");
        if (node == null)
            throw new IllegalStateException("XML foreignKey definition requires 'column' attribute");
        columnName = node.getNodeValue();
        node = attribs.getNamedItem("remoteSchema");
        remoteSchema = node == null ? null : node.getNodeValue();
        node = attribs.getNamedItem("remoteCatalog");
        remoteCatalog = node == null ? null : node.getNodeValue();

		LOGGER.debug("Found XML FK metadata for {}.{} remoteCatalog: {} remoteSchema: {}", tableName, columnName, remoteCatalog, remoteSchema);
    }

    public String getTableName() {
        return tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getRemoteCatalog() {
        return remoteCatalog;
    }

    public String getRemoteSchema() {
        return remoteSchema;
    }

    @Override
    public String toString() {
        return tableName + '.' + columnName;
    }
}