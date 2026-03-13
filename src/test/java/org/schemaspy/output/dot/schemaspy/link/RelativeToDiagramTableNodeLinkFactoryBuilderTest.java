package org.schemaspy.output.dot.schemaspy.link;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RelativeToDiagramTableNodeLinkFactoryBuilderTest {

    @Test
    void wrapReturnsTableNodeFactory() {
        assertThat(new RelativeToDiagramTableNodeLinkFactoryBuilder(null).withTableNodeLinkFactory((t) -> () -> "yepp")).isInstanceOf(TableNodeLinkFactory.class);
    }

}