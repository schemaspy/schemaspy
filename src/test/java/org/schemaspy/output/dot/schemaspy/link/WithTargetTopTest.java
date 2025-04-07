package org.schemaspy.output.dot.schemaspy.link;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WithTargetTopTest {

    @Test
    void containsTargetTop() {
        assertThat(new WithTargetTop(() -> "").asString()).contains("target=\"_top\"");
    }

    @Test
    void withUrlFromNodeLink() {
        assertThat(new WithTargetTop(() -> "table.html").asString()).contains("URL=\"table.html\"");
    }

}