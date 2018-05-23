/*
 * Copyright (C) 2004 - 2010 John Currier
 * Copyright (C) 2017 - Wojciech Kasa
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

import org.w3c.dom.Node;

/**
 * @author John Currier
 * @author Wojciech Kasa
 * @author Nils Petzaell
 */
public class DOMUtil {

    private DOMUtil() {
    }

    /**
     * Append the specified key/value pair of attributes to the <code>Node</code>.
     * @param node Node
     * @param name String
     * @param value String
     */
    public static void appendAttribute(Node node, String name, String value) {
        if (value != null) {
            Node attribute = node.getOwnerDocument().createAttribute(name);
            attribute.setNodeValue(value);
            node.getAttributes().setNamedItem(attribute);
        }
    }
}
