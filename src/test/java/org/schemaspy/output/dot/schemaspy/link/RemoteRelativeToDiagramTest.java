package org.schemaspy.output.dot.schemaspy.link;

import org.junit.jupiter.api.Test;
import org.schemaspy.model.Table;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RemoteRelativeToDiagramTest {

    @Test
    void backReference3TimesSchemaTablesTableA() {
        Table table = mock(Table.class);
        when(table.getContainer()).thenReturn("schema");
        assertThat(new RemoteRelativeToDiagram(table, () -> "tableA.html").asString()).isEqualTo("../../../schema/tables/tableA.html");
    }

}