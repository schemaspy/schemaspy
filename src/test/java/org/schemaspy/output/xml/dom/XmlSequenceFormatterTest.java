package org.schemaspy.output.xml.dom;

import org.junit.jupiter.api.Test;
import org.schemaspy.model.*;
import org.schemaspy.testing.XmlOutputDiff;
import org.xmlunit.builder.Input;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link XmlSequenceFormatter}.
 */
class XmlSequenceFormatterTest {

    /**
     * Given a coordinate in an XML documented, and
     * Given an empty collection of sequences,
     * When the object is asked to append the sequences,
     * Then it should leave the document unaltered.
     */
    @Test
    void ignoreEmpty() throws ParserConfigurationException {
        var doc = DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .newDocument();
        var element = doc.createElement("database");
        doc.appendChild(element);

        new XmlSequenceFormatter().appendSequences(
            element,
            Collections.emptyList()
        );

        var diff = XmlOutputDiff.diffXmlOutput(
            Input.fromDocument(doc),
            Input.fromString(
                """
                      <?xml version="1.0" encoding="UTF-8"?><database>
                      </database>"""
            ));
        assertThat(diff.getDifferences()).isEmpty();
    }

    /**
     * Given a coordinate in an XML documented, and
     * Given a collection of a single sequence,
     * When the object is asked to append the sequence,
     * Then it should add the sequence at the coordinate.
     */
    @Test
    void appendSequence() throws ParserConfigurationException {
        var doc = DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .newDocument();
        var element = doc.createElement("database");
        doc.appendChild(element);

        new XmlSequenceFormatter().appendSequences(
            element,
            List.of(
                new Sequence("foobar", 1, 15)
            )
        );

        var diff = XmlOutputDiff.diffXmlOutput(
            Input.fromDocument(doc),
            Input.fromString(
                """
                    <?xml version="1.0" encoding="UTF-8"?><database>
                         <sequences>
                            <sequence increment="15" name="foobar" startValue="1"/>
                         </sequences>
                      </database>"""
            ));
        assertThat(diff.getDifferences()).isEmpty();
    }

    /**
     * Given a coordinate in an XML documented, and
     * Given a collection of sequences,
     * When the object is asked to append the sequences,
     * Then it should add the sequences, alphabetically, at the coordinate.
     */
    @Test
    void sortSequences() throws ParserConfigurationException {
        var doc = DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .newDocument();
        var element = doc.createElement("database");
        doc.appendChild(element);

        new XmlSequenceFormatter().appendSequences(
            element,
            List.of(
                new Sequence("foo", 1, 3),
                new Sequence("bar", 1, 5)
            )
        );

        var diff = XmlOutputDiff.diffXmlOutput(
            Input.fromDocument(doc),
            Input.fromString(
                """
                    <?xml version="1.0" encoding="UTF-8"?><database>
                         <sequences>
                            <sequence increment="5" name="bar" startValue="1"/>
                            <sequence increment="3" name="foo" startValue="1"/>
                         </sequences>
                      </database>"""
            ));
        assertThat(diff.getDifferences()).isEmpty();
    }
}
