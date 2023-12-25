/*
 * Copyright (C) 2017 Wojciech Kasa
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
package org.schemaspy.input.dbms.xml;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Wojciech Kasa
 */
class TableColumnMetaTest {

    @Test
    void nullableShouldBeTrue() throws Exception {
        Element itemElement = initialiseElement();
        itemElement.setAttribute("nullable", "true");
        TableColumnMeta tableColumnMeta = new TableColumnMeta(itemElement);
        assertThat(tableColumnMeta.isNullable()).isTrue();
    }

    @Test
    void autoUpdatedShouldBeTrue() throws Exception {
        Element itemElement = initialiseElement();
        itemElement.setAttribute("autoUpdated", "true");
        TableColumnMeta tableColumnMeta = new TableColumnMeta(itemElement);
        assertThat(tableColumnMeta.isAutoUpdated()).isTrue();
    }

    @Test
    void primaryShouldBeTrue() throws Exception {
        Element itemElement = initialiseElement();
        itemElement.setAttribute("primaryKey", "true");
        TableColumnMeta tableColumnMeta = new TableColumnMeta(itemElement);
        assertThat(tableColumnMeta.isPrimary()).isTrue();
    }


    @Test
    void shouldDisableImpliedKeys() throws Exception {
        Element itemElement = initialiseElement();
        itemElement.setAttribute("disableImpliedKeys", "all");
        TableColumnMeta tableColumnMeta = new TableColumnMeta(itemElement);

        assertThat(tableColumnMeta.isImpliedChildrenDisabled()).isTrue();
        assertThat(tableColumnMeta.isImpliedParentsDisabled()).isTrue();

        itemElement.setAttribute("disableImpliedKeys", "to");
        tableColumnMeta = new TableColumnMeta(itemElement);

        assertThat(tableColumnMeta.isImpliedChildrenDisabled()).isTrue();
        assertThat(tableColumnMeta.isImpliedParentsDisabled()).isFalse();

        itemElement.setAttribute("disableImpliedKeys", "from");
        tableColumnMeta = new TableColumnMeta(itemElement);

        assertThat(tableColumnMeta.isImpliedChildrenDisabled()).isFalse();
        assertThat(tableColumnMeta.isImpliedParentsDisabled()).isTrue();

        itemElement.setAttribute("disableImpliedKeys", "other");
        tableColumnMeta = new TableColumnMeta(itemElement);

        assertThat(tableColumnMeta.isImpliedChildrenDisabled()).isFalse();
        assertThat(tableColumnMeta.isImpliedParentsDisabled()).isFalse();
    }

    @Test
    void shouldDisableDiagramAssociations() throws Exception {
        Element itemElement = initialiseElement();
        itemElement.setAttribute("disableDiagramAssociations", "all");
        TableColumnMeta tableColumnMeta = new TableColumnMeta(itemElement);

        assertThat(tableColumnMeta.isAllExcluded()).isTrue();
        assertThat(tableColumnMeta.isExcluded()).isTrue();

        itemElement.setAttribute("disableDiagramAssociations", "exceptdirect");
        tableColumnMeta = new TableColumnMeta(itemElement);

        assertThat(tableColumnMeta.isAllExcluded()).isFalse();
        assertThat(tableColumnMeta.isExcluded()).isTrue();

        itemElement.setAttribute("disableDiagramAssociations", "other");
        tableColumnMeta = new TableColumnMeta(itemElement);

        assertThat(tableColumnMeta.isAllExcluded()).isFalse();
        assertThat(tableColumnMeta.isExcluded()).isFalse();
    }


    private Element initialiseElement() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();

        Element element = document.createElement("root");
        document.appendChild(element);

        Element itemElement = document.createElement("item");
        element.appendChild(itemElement);

        itemElement.setAttribute("name", "name");
        return itemElement;
    }
}
