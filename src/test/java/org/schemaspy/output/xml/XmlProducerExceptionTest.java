package org.schemaspy.output.xml;

import org.junit.jupiter.api.Test;
import org.schemaspy.output.OutputException;

import javax.xml.parsers.ParserConfigurationException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link XmlProducerException}.
 */
class XmlProducerExceptionTest {

    /**
     * Given a message,
     * When the object is thrown,
     * Then it provides the message and type information.
     */
    @Test
    void provideDetails() {
        var message = "foo";
        assertThatThrownBy(() -> {
            throw new XmlProducerException(message, new ParserConfigurationException());
        }).isInstanceOf(OutputException.class)
            .hasMessage(message);
    }
}
