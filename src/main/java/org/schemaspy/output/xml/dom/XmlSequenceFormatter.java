/*
 * Copyright (C) 2018 AE Ibrahim
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

import org.schemaspy.model.Sequence;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

public class XmlSequenceFormatter {

    public void appendSequences(Element schemaNode, Collection<Sequence> sequences) {
        LinkedList<Sequence> sequencesList = new LinkedList<>(sequences);
        Collections.sort(sequencesList, Comparator.comparing(Sequence::getName));
        if (!sequencesList.isEmpty()) {
            Element sequencesElement = schemaNode.getOwnerDocument().createElement("sequences");
            schemaNode.appendChild(sequencesElement);
            for (Sequence sequence : sequencesList) {
                appendSequence(sequencesElement, sequence);
            }
        }
    }

    private static void appendSequence(Element sequencesElement, Sequence sequence) {
        Element sequenceElement = sequencesElement.getOwnerDocument().createElement("sequence");
        sequencesElement.appendChild(sequenceElement);
        DOMUtil.appendAttribute(sequenceElement, "name", sequence.getName());
        DOMUtil.appendAttribute(sequenceElement, "startValue", Integer.toString(sequence.getStartValue()));
        DOMUtil.appendAttribute(sequenceElement, "increment", Integer.toString(sequence.getIncrement()));
    }
}
