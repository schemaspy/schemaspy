package org.schemaspy.output.dot.schemaspy.link;

import org.junit.jupiter.api.Test;
import org.schemaspy.model.RemoteTable;
import org.schemaspy.model.Table;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RemoteFromBaseTest {

    @Test
    void backReferenceAndIncludeSchema() {
        Table table = mock(RemoteTable.class);
        when(table.getContainer()).thenReturn("schema");
        assertThat(new RemoteFromBase(table, () -> "tableA.html").asString()).isEqualTo("../schema/tables/tableA.html");
    }

}