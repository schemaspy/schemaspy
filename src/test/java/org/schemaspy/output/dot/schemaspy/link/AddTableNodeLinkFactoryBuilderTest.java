package org.schemaspy.output.dot.schemaspy.link;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AddTableNodeLinkFactoryBuilderTest {

    @Test
    void wrapReturnsATableNodeLinkFactory() {
        assertThat(new AddTableNodeLinkFactoryBuilder(true).withTableNodeLinkFactory((t) -> () -> "yepp")).isInstanceOf(TableNodeLinkFactory.class);
    }
}