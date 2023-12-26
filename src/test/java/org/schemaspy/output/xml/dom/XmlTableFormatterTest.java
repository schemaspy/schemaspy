/*
 * Copyright (C) 2018 Nils Petzaell
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
import org.schemaspy.testing.XmlOutputDiff;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Nils Petzaell
 */
class XmlTableFormatterTest {

    private static final XmlTableFormatter xmlTableFormatter = new XmlTableFormatter();

    @Test
    void withCheckConstraint() throws ParserConfigurationException, TransformerException {
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><database>\n" +
                "   <tables>\n" +
                "      <table catalog=\"catalog\" name=\"table\" numRows=\"0\" remarks=\"table\" schema=\"schema\" type=\"TABLE\">\n" +
                "         <checkConstraint constraint=\"check\" name=\"this is a\"/>\n" +
                "      </table>\n" +
                "   </tables>\n" +
                "</database>";

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element element = doc.createElement("database");
        doc.appendChild(element);

        Table table = new Table(mockDatabase("database"),"catalog", "schema", "table", "table");
        table.addCheckConstraint("this is a", "check");

        xmlTableFormatter.appendTables(element, Collections.singletonList(table));

        Diff diff = XmlOutputDiff.diffXmlOutput(
                Input.fromString(XmlHelp.toString(element)),
                Input.fromString(expected)
        );
        assertThat(diff.getDifferences()).isEmpty();

    }

    private Database mockDatabase(String databaseName) {
        Database database = mock(Database.class);
        when(database.getName()).thenReturn(databaseName);
        return database;
    }

}