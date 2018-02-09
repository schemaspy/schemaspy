package org.schemaspy.output.xml.dom;

import org.junit.Test;
import org.schemaspy.model.Database;
import org.schemaspy.model.Table;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class XmlTableFormatterTest {

    private static final String NEW_LINE = System.lineSeparator();

    private static final String CHECK_CONSTRAINT_EXPECT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><database>" + NEW_LINE +
            "   <tables>" + NEW_LINE +
            "      <table catalog=\"catalog\" name=\"table\" numRows=\"0\" remarks=\"table\" schema=\"schema\" type=\"TABLE\">" + NEW_LINE +
            "         <checkConstraint constraint=\"check\" name=\"this is a\"/>" + NEW_LINE +
            "      </table>" + NEW_LINE +
            "   </tables>" + NEW_LINE +
            "</database>" + NEW_LINE;

    @Test
    public void withCheckConstraint() throws ParserConfigurationException, TransformerException {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element element = doc.createElement("database");
        doc.appendChild(element);

        Table table = new Table(mockDatabase("database"),"catalog", "schema", "table", "table");
        table.addCheckConstraint("this is a", "check");

        XmlTableFormatter.getInstance().appendTables(element, Collections.singletonList(table));

        assertThat(toString(element)).isEqualTo(CHECK_CONSTRAINT_EXPECT);

    }

    private Database mockDatabase(String databaseName) {
        Database database = mock(Database.class);
        when(database.getName()).thenReturn(databaseName);
        return database;
    }

    private String toString(Element element) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(element),
                new StreamResult(byteArrayOutputStream));

        return byteArrayOutputStream.toString();
    }

}