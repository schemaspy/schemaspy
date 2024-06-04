/*
 * Copyright (C) 2004 - 2011 John Currier
 * Copyright (C) 2017 Wojciech Kasa
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
package org.schemaspy.input.dbms.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;


/**
 * Additional metadata about a column as expressed in XML instead of from
 * the database.
 *
 * @author John Currier
 * @author Wojciech Kasa
 * @author Daniel Watt
 * @author Nils Petzaell
 */
public class TableColumnMeta {
    private final Node colNode;
    private final String name;
    private final boolean isPrimary;
    private final String comments;
    private final List<ForeignKeyMeta> foreignKeys = new ArrayList<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public TableColumnMeta(Node colNode) {
        this.colNode = colNode;
        NamedNodeMap attribs = colNode.getAttributes();

        name = attribs.getNamedItem("name").getNodeValue();
        comments = new CmFacade(colNode).value();

        final Node node = attribs.getNamedItem("primaryKey");
        isPrimary = node != null && evalBoolean(node.getNodeValue());

		LOGGER.debug("Found XML column metadata for {} isPrimaryKey: {} comments: {}", name, isPrimary, comments);

        NodeList fkNodes = ((Element)colNode.getChildNodes()).getElementsByTagName("foreignKey");

        for (int i = 0; i < fkNodes.getLength(); ++i) {
            Node fkNode = fkNodes.item(i);
            foreignKeys.add(new ForeignKeyMeta(fkNode));
        }
    }

    private boolean evalBoolean(String exp) {
        if (exp == null)
            return false;

        String returnExp = exp.trim().toLowerCase();
        return "true".equals(returnExp) || "yes".equals(returnExp) || "1".equals(returnExp);
    }

    public String getName() {
        return name;
    }

    public String getType() {
        final Node node = this.colNode.getAttributes().getNamedItem("type");
        return node == null ? "Unknown" : node.getNodeValue();
    }

    public String getId() {
        final Node node = this.colNode.getAttributes().getNamedItem("id");
        return node == null ? null : node.getNodeValue();
    }

    public int getSize() {
        final Node node = this.colNode.getAttributes().getNamedItem("size");
        return node == null ? 0 : Integer.parseInt(node.getNodeValue());
    }

    public int getDigits() {
        final Node node = this.colNode.getAttributes().getNamedItem("digits");
        return node == null ? 0 : Integer.parseInt(node.getNodeValue());
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public boolean isNullable() {
        final Node node = this.colNode.getAttributes().getNamedItem("nullable");
        return node != null && evalBoolean(node.getNodeValue());
    }

    public boolean isAutoUpdated() {
        final Node node = this.colNode.getAttributes().getNamedItem("autoUpdated");
        return node != null && evalBoolean(node.getNodeValue());
    }

    public String getComments() {
        return comments;
    }

    public String getDefaultValue() {
        final Node node = this.colNode.getAttributes().getNamedItem("defaultValue");
        return node == null ? null : node.getNodeValue();
    }

    public List<ForeignKeyMeta> getForeignKeys() {
        return foreignKeys;
    }

    public boolean isExcluded() {
        final NamedNodeMap attribs = colNode.getAttributes();
        final boolean isExcluded;

        final Node node = attribs.getNamedItem("disableDiagramAssociations");
        if (node != null) {
            final String tmp = node.getNodeValue().trim().toLowerCase();
            switch (tmp) {
                case "all":
                case "exceptdirect":
                    isExcluded = true;
                    break;
                default:
                    isExcluded = false;
                    break;
            }
        } else {
            isExcluded = false;
        }
        return isExcluded;
    }

    public boolean isAllExcluded() {
        final NamedNodeMap attribs = colNode.getAttributes();
        final boolean isAllExcluded;

        final Node node = attribs.getNamedItem("disableDiagramAssociations");
        if (node != null) {
            final String tmp = node.getNodeValue().trim().toLowerCase();
            switch (tmp) {
                case "all":
                    isAllExcluded = true;
                    break;
                case "exceptdirect":
                default:
                    isAllExcluded = false;
                    break;
            }
        } else {
            isAllExcluded = false;
        }
        return isAllExcluded;
    }

    public boolean isImpliedParentsDisabled() {
        final NamedNodeMap attribs = colNode.getAttributes();
        final boolean isImpliedParentsDisabled;

        final Node node = attribs.getNamedItem("disableImpliedKeys");
        if (node != null) {
            final String tmp = node.getNodeValue().trim().toLowerCase();
            switch (tmp) {
                case "all":
                case "from":
                    isImpliedParentsDisabled = true;
                    break;
                case "to":
                default:
                    isImpliedParentsDisabled = false;
                    break;
            }
        } else {
            isImpliedParentsDisabled = false;
        }
        return isImpliedParentsDisabled;
    }

    public boolean isImpliedChildrenDisabled() {
        final NamedNodeMap attribs = this.colNode.getAttributes();
        final boolean isImpliedChildrenDisabled;

        final Node node = attribs.getNamedItem("disableImpliedKeys");
        if (node != null) {
            final String tmp = node.getNodeValue().trim().toLowerCase();
            switch (tmp) {
                case "all":
                case "to":
                    isImpliedChildrenDisabled = true;
                    break;
                case "from":
                default:
                    isImpliedChildrenDisabled = false;
                    break;
            }
        } else {
            isImpliedChildrenDisabled = false;
        }

        return isImpliedChildrenDisabled;
    }
}
