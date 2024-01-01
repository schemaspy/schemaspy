package org.schemaspy.util.markup;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.schemaspy.model.Database;
import org.schemaspy.model.LogicalTable;
import org.schemaspy.model.Table;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageRegistryTest {

    @Test
    void normalTablesCanBeAddedAndRetrieved() {

        Database database = Mockito.mock(Database.class);
        Mockito.when(database.getName()).thenReturn("database");
        assertThat(
            new PageRegistry().register(
                List.of(
                    new Table(database,"","", "tableÖ", "comment")
                )
            ).pathForPage("tableÖ")
        ).isNotNull();
    }

    @Test
    void logicalTablesCanNotBeAdded() {
        Database database = Mockito.mock(Database.class);
        Mockito.when(database.getName()).thenReturn("database");
        assertThat(
            new PageRegistry().register(
                List.of(
                    new LogicalTable(database,"","", "tableÖ", "comment")
                )
            ).pathForPage("tableÖ")
        ).isNull();
    }

}