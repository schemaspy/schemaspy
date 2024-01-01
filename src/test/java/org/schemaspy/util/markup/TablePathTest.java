package org.schemaspy.util.markup;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TablePathTest {

    @Test
    void startWithTables() {
        assertThat(
            new TablePath("tableA").value()
        ).startsWith("tables/");
    }

    @Test
    void endWithTables() {
        assertThat(
            new TablePath("tableA").value()
        ).endsWith(".html");
    }

    @Test
    void sanitizesName() {
        assertThat(
            new TablePath("tableÖ").value()
        ).contains("table__cb773ec8");
    }


}