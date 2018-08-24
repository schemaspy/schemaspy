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

import org.schemaspy.model.Routine;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

public class XmlRoutineFormatter {

    public void appendRoutines(Element schemaNode, Collection<Routine> routines) {
        LinkedList<Routine> routinesList = new LinkedList<>(routines);
        Collections.sort(routinesList, Comparator.comparing(Routine::getName));
        if (!routinesList.isEmpty()) {
            Element routinesElement = schemaNode.getOwnerDocument().createElement("routines");
            schemaNode.appendChild(routinesElement);
            for( Routine routine : routinesList) {
                appendRoutine(routinesElement, routine);
            }
        }
    }

    private static void appendRoutine(Element routinesElement, Routine routine) {
        Element routineElement = routinesElement.getOwnerDocument().createElement("routine");
        routinesElement.appendChild(routineElement);
        DOMUtil.appendAttribute(routineElement, "name", routine.getName());
        DOMUtil.appendAttribute(routineElement, "type", routine.getType());
        DOMUtil.appendAttribute(routineElement, "returnType", routine.getReturnType());
        DOMUtil.appendAttribute(routineElement, "dataAccess", routine.getDataAccess());
        DOMUtil.appendAttribute(routineElement, "securityType", routine.getSecurityType());
        DOMUtil.appendAttribute(routineElement, "deterministic", Boolean.toString(routine.isDeterministic()));
        Element commentElement = routinesElement.getOwnerDocument().createElement("comment");
        routineElement.appendChild(commentElement);
        if (notNullOrEmpty(routine.getComment())) {
           CDATASection commentCDATA = routinesElement.getOwnerDocument().createCDATASection(routine.getComment());
           commentElement.appendChild(commentCDATA);
        }
        Element definitionElement = routinesElement.getOwnerDocument().createElement("definition");
        routineElement.appendChild(definitionElement);
        if (notNullOrEmpty(routine.getDefinitionLanguage())) {
            DOMUtil.appendAttribute(definitionElement, "language", routine.getDefinitionLanguage());
        }
        if (notNullOrEmpty(routine.getDefinition())) {
            CDATASection definitionCDATA = routinesElement.getOwnerDocument().createCDATASection(routine.getDefinition());
            definitionElement.appendChild(definitionCDATA);
        }
    }

    private static boolean notNullOrEmpty(String string) {
        return string != null && !string.isEmpty();
    }
}
