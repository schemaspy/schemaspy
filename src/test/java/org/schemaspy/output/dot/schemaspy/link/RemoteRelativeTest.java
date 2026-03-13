package org.schemaspy.output.dot.schemaspy.link;

import org.junit.jupiter.api.Test;
import org.schemaspy.model.Table;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RemoteRelativeTest {

    @Test
    void backReference2TimesSchemaTablesTable() {
        Table table = mock(Table.class);
        when(table.getContainer()).thenReturn("schema");
        assertThat(new RemoteRelative(table, () -> "tableA.html").asString()).isEqualTo("../../schema/tables/tableA.html");
    }

}