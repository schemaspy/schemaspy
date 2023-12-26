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
import org.schemaspy.model.Routine;
import org.schemaspy.model.RoutineParameter;
import org.schemaspy.testing.XmlOutputDiff;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.Collection;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"java:S5976"})
class XmlRoutineFormatterTest {

    private static final XmlRoutineFormatter xmlRoutineFormatter = new XmlRoutineFormatter();

    @Test
    void noCommentNoDefinitionLanguageNoDefinition() throws ParserConfigurationException, TransformerException {
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><database>\n" +
                "   <routines>\n" +
                "      <routine dataAccess=\"MODIFIES\" deterministic=\"true\" name=\"noCommentNoDefinitionLanguageNoDefinition\" returnType=\"int\" securityType=\"INVOKER\" type=\"FUNCTION\">\n" +
                "         <comment/>\n" +
                "         <definition/>\n" +
                "         <parameters/>\n" +
                "      </routine>\n" +
                "   </routines>\n" +
                "</database>";

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element element = doc.createElement("database");
        doc.appendChild(element);
        Collection<Routine> routines = Collections.singletonList(
                new Routine("noCommentNoDefinitionLanguageNoDefinition",
                        "FUNCTION",
                        "int",
                        null,
                        null,
                        true,
                        "MODIFIES",
                        "INVOKER",
                        null)
        );
        xmlRoutineFormatter.appendRoutines(element, routines);
        Diff diff = XmlOutputDiff.diffXmlOutput(
                Input.fromString(XmlHelp.toString(element)),
                Input.fromString(expected)
        );
        assertThat(diff.getDifferences()).isEmpty();
    }

    @Test
    void noCommentNoDefinitionLanguage() throws ParserConfigurationException, TransformerException {
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><database>\n" +
                "   <routines>\n" +
                "      <routine dataAccess=\"MODIFIES\" deterministic=\"true\" name=\"noCommentNoDefinitionLanguageNoDefinition\" returnType=\"int\" securityType=\"INVOKER\" type=\"FUNCTION\">\n" +
                "         <comment/>\n" +
                "         <definition><![CDATA[something < 10 else]]></definition>\n" +
                "         <parameters/>\n" +
                "      </routine>\n" +
                "   </routines>\n" +
                "</database>";

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element element = doc.createElement("database");
        doc.appendChild(element);
        Collection<Routine> routines = Collections.singletonList(
                new Routine("noCommentNoDefinitionLanguageNoDefinition",
                        "FUNCTION",
                        "int",
                        null,
                        "something < 10 else",
                        true,
                        "MODIFIES",
                        "INVOKER",
                        null)
        );
        xmlRoutineFormatter.appendRoutines(element, routines);
        Diff diff = XmlOutputDiff.diffXmlOutput(
                Input.fromString(XmlHelp.toString(element)),
                Input.fromString(expected)
        );
        assertThat(diff.getDifferences()).isEmpty();
    }

    @Test
    void noComment() throws ParserConfigurationException, TransformerException {
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><database>\n" +
                "   <routines>\n" +
                "      <routine dataAccess=\"MODIFIES\" deterministic=\"true\" name=\"noCommentNoDefinitionLanguageNoDefinition\" returnType=\"int\" securityType=\"INVOKER\" type=\"FUNCTION\">\n" +
                "         <comment/>\n" +
                "         <definition language=\"SQL\"><![CDATA[something < 10 else]]></definition>\n" +
                "         <parameters/>\n" +
                "      </routine>\n" +
                "   </routines>\n" +
                "</database>";

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element element = doc.createElement("database");
        doc.appendChild(element);
        Collection<Routine> routines = Collections.singletonList(
                new Routine("noCommentNoDefinitionLanguageNoDefinition",
                        "FUNCTION",
                        "int",
                        "SQL",
                        "something < 10 else",
                        true,
                        "MODIFIES",
                        "INVOKER",
                        null)
        );
        xmlRoutineFormatter.appendRoutines(element, routines);
        Diff diff = XmlOutputDiff.diffXmlOutput(
                Input.fromString(XmlHelp.toString(element)),
                Input.fromString(expected)
        );
        assertThat(diff.getDifferences()).isEmpty();
    }

    @Test
    void full() throws TransformerException, ParserConfigurationException {
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><database>\n" +
                "   <routines>\n" +
                "      <routine dataAccess=\"MODIFIES\" deterministic=\"true\" name=\"noCommentNoDefinitionLanguageNoDefinition\" returnType=\"int\" securityType=\"INVOKER\" type=\"FUNCTION\">\n" +
                "         <comment><![CDATA[<html>]]></comment>\n" +
                "         <definition language=\"SQL\"><![CDATA[something < 10 else]]></definition>\n" +
                "         <parameters>\n" +
                "             <parameter name=\"myVar\" type=\"varchar\" mode=\"IN\"/>\n" +
                "             <parameter type=\"varchar\" mode=\"OUT\"/>\n" +
                "         </parameters>\n" +
                "      </routine>\n" +
                "   </routines>\n" +
                "</database>";

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element element = doc.createElement("database");
        doc.appendChild(element);
        Routine routine = new Routine("noCommentNoDefinitionLanguageNoDefinition",
                "FUNCTION",
                "int",
                "SQL",
                "something < 10 else",
                true,
                "MODIFIES",
                "INVOKER",
                "<html>");
        routine.addParameter(new RoutineParameter("myVar", "varchar", "IN"));
        routine.addParameter(new RoutineParameter("", "varchar", "OUT"));
        Collection<Routine> routines = Collections.singletonList(routine);
        xmlRoutineFormatter.appendRoutines(element, routines);
        Diff diff = XmlOutputDiff.diffXmlOutput(
                Input.fromString(XmlHelp.toString(element)),
                Input.fromString(expected)
        );
        assertThat(diff.getDifferences()).isEmpty();
    }
}