package org.schemaspy.output.dot.schemaspy.link;

import org.junit.jupiter.api.Test;
import org.schemaspy.model.Table;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AddTableNodeLinkFactoryTest {

    @Test
    void returnDelegateIfMultiSchema() {
        Table table = mock(Table.class);
        when(table.isRemote()).thenReturn(true);
        assertThat(new AddTableNodeLinkFactory(true, (t) -> () -> "yepp").nodeLink(table).asString()).contains("yepp");
    }

    @Test
    void returnDelegateIfNotRemote() {
        Table table = mock(Table.class);
        when(table.isRemote()).thenReturn(false);
        assertThat(new AddTableNodeLinkFactory(false, (t) -> () -> "yepp").nodeLink(table).asString()).contains("yepp");
    }

    @Test
    void returnNoNodeLinkIfRemoteAndSingleSchema() {
        Table table = mock(Table.class);
        when(table.isRemote()).thenReturn(true);
        assertThat(new AddTableNodeLinkFactory(false, (t) -> () -> "yepp").nodeLink(table)).isInstanceOf(NoNodeLink.class);
    }

}