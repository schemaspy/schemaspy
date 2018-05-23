/*
 * Copyright (C) 2004 - 2010 John Currier
 * Copyright (C) 2017 Wojciech Kasa
 * Copyright (C) 2017 Daniel Watt
 * Copyright (C) 2017, 2018 Nils Petzaell
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

import org.schemaspy.Config;
import org.schemaspy.model.InvalidConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

/**
 * Additional metadata about a schema as expressed in XML instead of from
 * the database.
 *
 * @author John Currier
 * @author Wojciech Kasa
 * @author Daniel Watt
 * @author Nils Petzaell
 */
public class SchemaMeta {
    private final List<TableMeta> tables = new ArrayList<TableMeta>();
    private final String comments;
    private final File metaFile;
    private final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public SchemaMeta(String xmlMeta, String dbName, String schema) throws InvalidConfigurationException {
        File meta = new File(xmlMeta);
        if (meta.isDirectory()) {
            String filename = (schema == null ? dbName : schema) + ".meta.xml";
            meta = new File(meta, filename);

            if (!meta.exists()) {
                if (Config.getInstance().isOneOfMultipleSchemas()) {
                    // don't force all of the "one of many" schemas to have metafiles
                    LOGGER.info("Meta directory \"{}\" should contain a file named \"{}" + '\"', xmlMeta, filename);
                    comments = null;
                    metaFile = null;
                    return;
                }

                throw new InvalidConfigurationException("Meta directory \"" + xmlMeta + "\" must contain a file named \"" + filename + '\"');
            }
        } else if (!meta.exists()) {
            throw new InvalidConfigurationException("Specified meta file \"" + xmlMeta + "\" does not exist");
        }

        metaFile = meta;

        Document doc = parse(metaFile);

        NodeList commentsNodes = doc.getElementsByTagName("comments");
        if (commentsNodes != null && commentsNodes.getLength() > 0 && commentsNodes.item(0).hasChildNodes())
            comments = commentsNodes.item(0).getTextContent();
        else
            comments = null;

        NodeList tablesNodes = doc.getElementsByTagName("tables");
        if (tablesNodes != null) {
            NodeList tableNodes = ((Element)tablesNodes.item(0)).getElementsByTagName("table");

            for (int i = 0; i < tableNodes.getLength(); ++i) {
                Node tableNode = tableNodes.item(i);
                TableMeta tableMeta = new TableMeta(tableNode);
                tables.add(tableMeta);
            }
        }
    }

    /**
     * Comments that describe the schema
     */
    public String getComments() {
        return comments;
    }

    public File getFile() {
        return metaFile;
    }

    public List<TableMeta> getTables() {
        return tables;
    }

    private void validate(Document document) throws SAXException, IOException {
        // create a SchemaFactory capable of understanding WXS schemas
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        // load a WXS schema, represented by a Schema instance
        InputStream xsl = getClass().getResourceAsStream("/schemaspy.meta.xsd");

        Schema schema = factory.newSchema(new StreamSource(xsl));

        // create a Validator instance, which can be used to validate an instance document
        Validator validator = schema.newValidator();

        // validate the DOM tree
        validator.validate(new DOMSource(document));
    }

    private Document parse(File file) throws InvalidConfigurationException {
        DocumentBuilder docBuilder;
        Document doc;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setIgnoringElementContentWhitespace(true);
        factory.setIgnoringComments(true);

        try {
            docBuilder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException exc) {
            throw new InvalidConfigurationException("Invalid XML parser configuration", exc);
        }

        try {
            LOGGER.info("Parsing {}", file);
            doc = docBuilder.parse(file);
        } catch (SAXException exc) {
            throw new InvalidConfigurationException("Failed to parse " + file, exc);
        } catch (IOException exc) {
            throw new InvalidConfigurationException("Could not read " + file + ":", exc);
        }
        try {
            validate(doc);
        } catch (SAXException | IOException exc) {
            LOGGER.warn("Failed to validate {}: {}", file, exc);
        }

        return doc;
    }
}