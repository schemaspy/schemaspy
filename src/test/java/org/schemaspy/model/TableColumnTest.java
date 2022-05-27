package org.schemaspy.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class TableColumnTest {

    @Test
    void noImplied() {
        TableColumn tableColumn = new TableColumn(mock(Table.class));
        tableColumn.setName("middle");

        TableColumn parent = new TableColumn(mock(Table.class));
        parent.setName("parent");
        tableColumn.addParent(parent, new ForeignKeyConstraint(parent, tableColumn));

        TableColumn child = new TableColumn(mock(Table.class));
        child.setName("child");
        tableColumn.addChild(child, new ForeignKeyConstraint(tableColumn, child));

        assertThat(tableColumn.hasImpliedConstraint()).isFalse();
    }

    @Test
    void parentImplied() {
        TableColumn tableColumn = new TableColumn(mock(Table.class));
        tableColumn.setName("child");
        TableColumn parent = new TableColumn(mock(Table.class));
        parent.setName("parent");

        tableColumn.addParent(parent, new ImpliedForeignKeyConstraint(parent, tableColumn));
        assertThat(tableColumn.hasImpliedConstraint()).isTrue();
    }

    @Test
    void childImplied() {
        TableColumn tableColumn = new TableColumn(mock(Table.class));
        tableColumn.setName("parent");
        TableColumn child = new TableColumn(mock(Table.class));
        child.setName("child");

        tableColumn.addChild(child, new ImpliedForeignKeyConstraint(tableColumn, child));
        assertThat(tableColumn.hasImpliedConstraint()).isTrue();
    }
}