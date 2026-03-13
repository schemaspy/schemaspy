package org.schemaspy.output.dot.schemaspy.link;

import org.junit.jupiter.api.Test;
import org.schemaspy.model.Table;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class WithTargetTopTableNodeLinkFactoryTest {

    @Test
    void wrapNodeLinkInWithTargetTop() {
        assertThat(new WithTargetTopTableNodeLinkFactory((t) -> () -> "yepp").nodeLink(mock(Table.class))).isInstanceOf(WithTargetTop.class);
    }

}