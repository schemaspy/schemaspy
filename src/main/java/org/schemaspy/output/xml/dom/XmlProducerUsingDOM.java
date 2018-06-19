/*
 * Copyright (C) 2004 - 2011 John Currier
 * Copyright (C) 2017 Wojciech Kasa
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

import org.schemaspy.model.Database;
import org.schemaspy.model.Table;
import org.schemaspy.output.xml.XmlProducer;
import org.schemaspy.output.xml.XmlProducerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * @author John Currier
 * @author Wojciech Kasa
 * @author Nils Petzaell
 */
public class XmlProducerUsingDOM implements XmlProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public void generate(Database database, File outputDir) {
        Collection<Table> tables = new ArrayList<>(database.getTables());
        tables.addAll(database.getViews());

        if (tables.isEmpty()) {
            LOGGER.info("No tables to output, nothing written to disk");
            return;
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException exc) {
            throw new XmlProducerException("Failed to get a newDocumentBuilder()",exc);
        }

        Document document = builder.newDocument();
        Element rootNode = document.createElement("database");
        document.appendChild(rootNode);
        DOMUtil.appendAttribute(rootNode, "name", database.getName());
        if (Objects.nonNull(database.getSchema()))
            DOMUtil.appendAttribute(rootNode, "schema", database.getSchema().getName());
        DOMUtil.appendAttribute(rootNode, "type", database.getDatabaseProduct());

        XmlTableFormatter.getInstance().appendTables(rootNode, tables);

        String xmlName = database.getName();

        // some dbNames have path info in the name...strip it
        xmlName = new File(xmlName).getName();

        // some dbNames include jdbc driver details including :'s and @'s
        String[] unusables = xmlName.split("[:@]");
        xmlName = unusables[unusables.length - 1];

        if (Objects.nonNull(database.getSchema()))
            xmlName += '.' + database.getSchema().getName();

        document.getDocumentElement().normalize();
        Path xmlFile = outputDir.toPath().resolve(xmlName + ".xml");
        try (Writer writer = Files.newBufferedWriter(xmlFile, StandardCharsets.UTF_8)){
            write(document, writer);
        } catch (IOException e) {
            throw new XmlProducerException("Unable to write xml to disk", e);
        } catch (TransformerException e) {
            throw new XmlProducerException("Unable to transform dom document to xml", e);
        }
    }

    private void write(Document document, Writer writer) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");

        transformer.transform(new DOMSource(document),
                new StreamResult(writer));
    }
}
