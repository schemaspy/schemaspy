package org.schemaspy.output.dot.schemaspy.link;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NoNodeLinkTest {

    @Test
    void shouldAlwaysBeEmpty() {
        assertThat(new NoNodeLink().asString()).isEmpty();
    }
}