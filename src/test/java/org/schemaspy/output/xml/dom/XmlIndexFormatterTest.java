/*
 * Copyright (C) 2019 Nils Petzaell
 *
 * This file is part of SchemaSpy.
 *
 * SchemaSpy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SchemaSpy. If not, see <http://www.gnu.org/licenses/>.
 */
package org.schemaspy.output.xml.dom;

import org.junit.jupiter.api.Test;
import org.schemaspy.model.Database;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.schemaspy.model.TableIndex;
import org.schemaspy.testing.XmlOutputDiff;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class XmlIndexFormatterTest {

    private final XmlIndexFormatter xmlIndexFormatter = new XmlIndexFormatter();

    @Test
    void appendIndex() throws ParserConfigurationException {

        String expecting = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><table>\n" +
                "   <index name=\"test\" unique=\"false\">\n" +
                "      <column ascending=\"false\" name=\"first\"/>\n" +
                "      <column ascending=\"true\" name=\"second\"/>\n" +
                "   </index>\n" +
                "</table>";

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element element = doc.createElement("table");
        doc.appendChild(element);

        Table mainTable = new Table(mockDatabase("database"),"catalog", "schema", "mainTable", "mainTable");
        TableColumn firstTableColumn = new TableColumn(mainTable);
        firstTableColumn.setId(0);
        firstTableColumn.setName("first");
        firstTableColumn.setLength(10);
        firstTableColumn.setTypeName("int");
        firstTableColumn.setNullable(false);
        firstTableColumn.setIsAutoUpdated(true);
        TableColumn secondTableColumn = new TableColumn(mainTable);
        secondTableColumn.setId(1);
        secondTableColumn.setName("second");
        secondTableColumn.setLength(10);
        secondTableColumn.setTypeName("int");
        secondTableColumn.setNullable(false);
        secondTableColumn.setIsAutoUpdated(false);

        TableIndex tableIndex = new TableIndex("test", false);
        tableIndex.addColumn(firstTableColumn, "D");
        tableIndex.addColumn(secondTableColumn, "A");

        xmlIndexFormatter.appendIndex(element,tableIndex, false);

        Diff diff = XmlOutputDiff.diffXmlOutput(Input.fromDocument(doc), Input.fromString(expecting));
        assertThat(diff.getDifferences()).isEmpty();
    }


    private Database mockDatabase(String databaseName) {
        Database database = mock(Database.class);
        when(database.getName()).thenReturn(databaseName);
        return database;
    }

}
