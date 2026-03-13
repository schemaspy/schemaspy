package org.schemaspy.output.dot.schemaspy.link;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RelativeTest {

    @Test
    void relativeIsJustTheNodeLink() {
        assertThat(new Relative(()-> "tableA.html").asString()).contains("tableA.html");
    }
}