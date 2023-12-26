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
import org.schemaspy.model.ForeignKeyConstraint;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.schemaspy.testing.XmlOutputDiff;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.sql.Types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class XmlColumnFormatterTest {

    private final XmlColumnFormatter xmlColumnFormatter = new XmlColumnFormatter();

    @Test
    void appendColumn() throws ParserConfigurationException, TransformerException {
        String expected ="<?xml version=\"1.0\" encoding=\"UTF-8\"?><table>\n" +
                "   <column autoUpdated=\"true\" digits=\"0\" id=\"0\" name=\"main\" nullable=\"false\" remarks=\"\" size=\"10\" type=\"int\" typeCode=\"4\" defaultValue=\"1\">\n" +
                "      <child catalog=\"catalog\" column=\"child\" implied=\"false\" onDeleteCascade=\"false\" schema=\"schema\" table=\"childTable\"/>\n" +
                "   </column>\n" +
                "   <column autoUpdated=\"false\" digits=\"0\" id=\"1\" name=\"fromParent\" nullable=\"false\" remarks=\"\" size=\"10\" type=\"int\" typeCode=\"4\" defaultValue=\"2\">\n" +
                "      <parent catalog=\"catalog\" column=\"parent\" implied=\"false\" onDeleteCascade=\"false\" schema=\"schema\" table=\"parentTable\"/>\n" +
                "   </column>\n" +
                "</table>";

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element element = doc.createElement("table");
        doc.appendChild(element);

        Table mainTable = new Table(mockDatabase("database"),"catalog", "schema", "mainTable", "mainTable");
        TableColumn mainTableColumn = new TableColumn(mainTable);
        mainTableColumn.setId(0);
        mainTableColumn.setName("main");
        mainTableColumn.setLength(10);
        mainTableColumn.setTypeName("int");
        mainTableColumn.setType(Types.INTEGER);
        mainTableColumn.setDefaultValue(1);
        mainTableColumn.setNullable(false);
        mainTableColumn.setIsAutoUpdated(true);
        TableColumn mainFromTableColumn = new TableColumn(mainTable);
        mainFromTableColumn.setId(1);
        mainFromTableColumn.setName("fromParent");
        mainFromTableColumn.setLength(10);
        mainFromTableColumn.setTypeName("int");
        mainFromTableColumn.setType(Types.INTEGER);
        mainFromTableColumn.setDefaultValue(2);
        mainFromTableColumn.setNullable(false);
        mainFromTableColumn.setIsAutoUpdated(false);

        Table childTable = new Table(mockDatabase("database"),"catalog", "schema", "childTable", "childTable");
        TableColumn childTableColumn = new TableColumn(childTable);
        childTableColumn.setId(0);
        childTableColumn.setName("child");
        childTableColumn.setLength(10);
        childTableColumn.setTypeName("int");
        childTableColumn.setNullable(false);
        childTableColumn.setIsAutoUpdated(false);

        Table parentTable = new Table(mockDatabase("database"),"catalog", "schema", "parentTable", "parentTable");
        TableColumn parentTableColumn = new TableColumn(parentTable);
        parentTableColumn.setId(0);
        parentTableColumn.setName("parent");
        parentTableColumn.setLength(10);
        parentTableColumn.setTypeName("int");
        parentTableColumn.setNullable(false);
        parentTableColumn.setIsAutoUpdated(true);

        new ForeignKeyConstraint(mainTableColumn, childTableColumn);
        new ForeignKeyConstraint(parentTableColumn, mainFromTableColumn);

        xmlColumnFormatter.appendColumn(element, mainTableColumn);
        xmlColumnFormatter.appendColumn(element, mainFromTableColumn);

        String xml = XmlHelp.toString(element);

        Diff diff = XmlOutputDiff.diffXmlOutput(Input.fromDocument(doc), Input.fromString(expected));
        assertThat(diff.getDifferences()).isEmpty();
    }

    private Database mockDatabase(String databaseName) {
        Database database = mock(Database.class);
        when(database.getName()).thenReturn(databaseName);
        return database;
    }

}