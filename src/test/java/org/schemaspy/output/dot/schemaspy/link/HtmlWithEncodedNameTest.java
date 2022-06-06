package org.schemaspy.output.dot.schemaspy.link;

import org.junit.jupiter.api.Test;
import org.schemaspy.model.Table;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HtmlWithEncodedNameTest {

    @Test
    void replacesSpacesWithPercentage20() {
        Table table = mock(Table.class);
        when(table.getName()).thenReturn("table A");
        assertThat(new HtmlWithEncodedName(table).asString()).isEqualTo("table_A_a370846f.html");
    }

}