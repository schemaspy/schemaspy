package org.schemaspy.output.dot.schemaspy.link;

import org.junit.jupiter.api.Test;
import org.schemaspy.model.Table;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FromBaseTableNodeLinkFactoryTest {

    @Test
    void remoteTable() {
        Table table = mock(Table.class);
        when(table.isRemote()).thenReturn(true);
        when(table.getName()).thenReturn("remoteTable");
        when(table.getContainer()).thenReturn("schema");
        assertThat(new FromBaseTableNodeLinkFactory().nodeLink(table).asString()).isEqualTo("../schema/tables/remoteTable.html");
    }

    @Test
    void localTable() {
        Table table = mock(Table.class);
        when(table.isRemote()).thenReturn(false);
        when(table.getName()).thenReturn("localTable");
        assertThat(new FromBaseTableNodeLinkFactory().nodeLink(table).asString()).isEqualTo("tables/localTable.html");
    }
}