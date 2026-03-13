package org.schemaspy.output.dot.schemaspy.link;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.schemaspy.model.Table;
import org.schemaspy.output.diagram.DiagramFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RelativeToDiagramTableNodeLinkFactoryTest {

    private final DiagramFactory usingSvg = mock(DiagramFactory.class);
    private final DiagramFactory usingPng = mock(DiagramFactory.class);

    @BeforeEach
    void setupMocks() {
        when(usingSvg.getDiagramFormat()).thenReturn("svg");
        when(usingPng.getDiagramFormat()).thenReturn("png");
    }

    @Test
    void remoteTableUsingSVG() {
        Table table = mock(Table.class);
        when(table.isRemote()).thenReturn(true);
        when(table.getName()).thenReturn("remoteTable");
        when(table.getContainer()).thenReturn("schema");
        assertThat(new RelativeToDiagramTableNodeLinkFactory(usingSvg, null).nodeLink(table).asString()).isEqualTo("../../../schema/tables/remoteTable.html");
    }

    @Test
    void localTableUsingSVG() {
        Table table = mock(Table.class);
        when(table.isRemote()).thenReturn(false);
        when(table.getName()).thenReturn("localTable");
        assertThat(new RelativeToDiagramTableNodeLinkFactory(usingSvg, null).nodeLink(table).asString()).isEqualTo("../../tables/localTable.html");
    }

    @Test
    void delegatesIfNotSVG() {
        Table table = mock(Table.class);
        when(table.isRemote()).thenReturn(false);
        when(table.getName()).thenReturn("localTable");
        assertThat(new RelativeToDiagramTableNodeLinkFactory(usingPng, (t) -> () -> "yepp").nodeLink(table).asString()).isEqualTo("yepp");
    }
}