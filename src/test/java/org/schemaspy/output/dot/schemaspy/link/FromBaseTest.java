package org.schemaspy.output.dot.schemaspy.link;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FromBaseTest {

    @Test
    void pathIsFromBase() {
        assertThat(new FromBase(() -> "tableA.html").asString()).contains("tables/tableA.html");
    }
}