package org.schemaspy.model;

import org.junit.Assert;
import org.junit.Test;
import org.schemaspy.model.xml.TableColumnMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by wkasa on 2017-05-08.
 */
public class TableColumnMetaTest {

    @Test
    public void nullableShouldBeTrue() throws Exception {
        Element itemElement = initaliseElement();
        itemElement.setAttribute("nullable", "true");
        TableColumnMeta tableColumnMeta = new TableColumnMeta(itemElement);

        Assert.assertEquals(true, tableColumnMeta.isNullable());

    }

    @Test
    public void autoUpdatedShouldBeTrue() throws Exception {
        Element itemElement = initaliseElement();
        itemElement.setAttribute("autoUpdated", "true");
        TableColumnMeta tableColumnMeta = new TableColumnMeta(itemElement);

        Assert.assertEquals(true, tableColumnMeta.isAutoUpdated());
    }

    @Test
    public void primaryShouldBeTrue() throws Exception {
        Element itemElement = initaliseElement();
        itemElement.setAttribute("primaryKey", "true");
        TableColumnMeta tableColumnMeta = new TableColumnMeta(itemElement);

        Assert.assertEquals(true, tableColumnMeta.isPrimary());
    }


    @Test
    public void shouldDisableImpliedKeys() throws Exception {
        Element itemElement = initaliseElement();
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
        Element itemElement = initaliseElement();
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


    private Element initaliseElement() throws ParserConfigurationException {
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
