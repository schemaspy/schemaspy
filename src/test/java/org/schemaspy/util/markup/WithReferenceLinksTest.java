package org.schemaspy.util.markup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.schemaspy.model.Table;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WithReferenceLinksTest {

    private final Table table1 = Mockito.mock(Table.class);
    private final Table table2 = Mockito.mock(Table.class);

    private final List<Table> tableList = List.of(table1, table2);

    @BeforeEach
    void setupTables() {
        Mockito.when(table1.getName()).thenReturn("table1");
        Mockito.when(table2.getName()).thenReturn("table2");
    }

    @Test
    void replaceLinksWithExistingTablesAndDefaultRootPathTest() {
        String sourceMarkdown = "See [table1] or [table2.column]";
        String expected =
            "See [table1](./tables/table1.html) or [table2.column](./tables/table2.html#column)";

        Mockito.when(table1.isLogical()).thenReturn(false);
        Mockito.when(table2.isLogical()).thenReturn(false);

        assertThat(
            new WithReferenceLinks(
                new PageRegistry().register(tableList),
                sourceMarkdown,
                "",
                "[%1$s](%2$s)"
            ).value()
        ).contains(expected);
    }

    @Test
    void replaceLinksWithExistingTablesAndOtherRootPathTest() {
        String sourceMarkdown = "See [table1] or [table2.column]";
        String expected =
            "See [table1](../root/tables/table1.html) or [table2.column](../root/tables/table2.html#column)";

        Mockito.when(table1.isLogical()).thenReturn(false);
        Mockito.when(table2.isLogical()).thenReturn(false);

        assertThat(
            new WithReferenceLinks(
                new PageRegistry().register(tableList),
                sourceMarkdown,
                "../root",
                "[%1$s](%2$s)"
            ).value()
        ).contains(expected);
    }

    @Test
    void doNotReplaceLinksWhenTableDoesNotExists() {
        String sourceMarkdown = "See [table1.column]";
        String expected = "See [table1.column]";

        Mockito.when(table1.isLogical()).thenReturn(true);
        Mockito.when(table2.isLogical()).thenReturn(true);

        assertThat(
            new WithReferenceLinks(
                new PageRegistry().register(tableList),
                sourceMarkdown,
                "",
                "[%1$s](%2$s)"
            ).value()
        ).contains(expected);
    }

}