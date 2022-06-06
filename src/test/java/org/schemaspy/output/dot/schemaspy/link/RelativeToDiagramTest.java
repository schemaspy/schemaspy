package org.schemaspy.output.dot.schemaspy.link;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RelativeToDiagramTest {

    @Test
    void referenceTwoLevelsUpThenTables() {
        assertThat(new RelativeToDiagram(() -> "tableA.html").asString()).isEqualTo("../../tables/tableA.html");
    }

}