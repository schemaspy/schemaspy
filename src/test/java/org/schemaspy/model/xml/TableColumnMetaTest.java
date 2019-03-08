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
package org.schemaspy.model.xml;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * @author Wojciech Kasa
 */
public class TableColumnMetaTest {

    @Test
    public void nullableShouldBeTrue() throws Exception {
        Element itemElement = initialiseElement();
        itemElement.setAttribute("nullable", "true");
        TableColumnMeta tableColumnMeta = new TableColumnMeta(itemElement);

        Assert.assertEquals(true, tableColumnMeta.isNullable());

    }

    @Test
    public void autoUpdatedShouldBeTrue() throws Exception {
        Element itemElement = initialiseElement();
        itemElement.setAttribute("autoUpdated", "true");
        TableColumnMeta tableColumnMeta = new TableColumnMeta(itemElement);

        Assert.assertEquals(true, tableColumnMeta.isAutoUpdated());
    }

    @Test
    public void primaryShouldBeTrue() throws Exception {
        Element itemElement = initialiseElement();
        itemElement.setAttribute("primaryKey", "true");
        TableColumnMeta tableColumnMeta = new TableColumnMeta(itemElement);

        Assert.assertEquals(true, tableColumnMeta.isPrimary());
    }


    @Test
    public void shouldDisableImpliedKeys() throws Exception {
        Element itemElement = initialiseElement();
        itemElement.setAttribute("disableImpliedKeys", "all");
        TableColumnMeta tableColumnMeta = new TableColumnMeta(itemElement);

        Assert.assertEquals(true, tableColumnMeta.isImpliedChildrenDisabled());
        Assert.assertEquals(true, tableColumnMeta.isImpliedParentsDisabled());

        itemElement.setAttribute("disableImpliedKeys", "to");
        tableColumnMeta = new TableColumnMeta(itemElement);

        Assert.assertEquals(true, tableColumnMeta.isImpliedChildrenDisabled());
        Assert.assertEquals(false, tableColumnMeta.isImpliedParentsDisabled());

        itemElement.setAttribute("disableImpliedKeys", "from");
        tableColumnMeta = new TableColumnMeta(itemElement);

        Assert.assertEquals(false, tableColumnMeta.isImpliedChildrenDisabled());
        Assert.assertEquals(true, tableColumnMeta.isImpliedParentsDisabled());

        itemElement.setAttribute("disableImpliedKeys", "other");
        tableColumnMeta = new TableColumnMeta(itemElement);

        Assert.assertEquals(false, tableColumnMeta.isImpliedChildrenDisabled());
        Assert.assertEquals(false, tableColumnMeta.isImpliedParentsDisabled());
    }

    @Test
    public void shouldDisableDiagramAssociations() throws Exception {
        Element itemElement = initialiseElement();
        itemElement.setAttribute("disableDiagramAssociations", "all");
        TableColumnMeta tableColumnMeta = new TableColumnMeta(itemElement);

        Assert.assertEquals(true, tableColumnMeta.isAllExcluded());
        Assert.assertEquals(true, tableColumnMeta.isExcluded());

        itemElement.setAttribute("disableDiagramAssociations", "exceptdirect");
        tableColumnMeta = new TableColumnMeta(itemElement);

        Assert.assertEquals(false, tableColumnMeta.isAllExcluded());
        Assert.assertEquals(true, tableColumnMeta.isExcluded());

        itemElement.setAttribute("disableDiagramAssociations", "other");
        tableColumnMeta = new TableColumnMeta(itemElement);

        Assert.assertEquals(false, tableColumnMeta.isAllExcluded());
        Assert.assertEquals(false, tableColumnMeta.isExcluded());
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
