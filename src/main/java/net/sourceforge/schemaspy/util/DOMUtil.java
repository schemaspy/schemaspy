/*
 * This file is a part of the SchemaSpy project (http://schemaspy.sourceforge.net).
 * Copyright (C) 2004, 2005, 2006, 2007, 2008, 2009, 2010 John Currier
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
package net.sourceforge.schemaspy.util;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Node;

public class DOMUtil {
    public static void printDOM(Node node, LineWriter out) throws TransformerException {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer xformer;
        boolean indentSpecified = false;

        // see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6296446
        // for issues about transformations in Java 5.x
        try {
            // won't work pre-5.x
            factory.setAttribute("indent-number", new Integer(3));
            indentSpecified = true;
        } catch (IllegalArgumentException factoryDoesntSupportIndentNumber) {
        }

        xformer = factory.newTransformer();
        xformer.setOutputProperty(OutputKeys.INDENT, "yes");
        if (!indentSpecified)
            xformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");

        xformer.transform(new DOMSource(node), new StreamResult(out));
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
