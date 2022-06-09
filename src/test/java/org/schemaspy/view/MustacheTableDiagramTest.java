package org.schemaspy.view;

import org.junit.jupiter.api.Test;
import org.schemaspy.output.diagram.DiagramResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MustacheTableDiagramTest {

    @Test
    void translateDiagramResults() {
        DiagramResult results = mock(DiagramResult.class);
        when(results.getFileName()).thenReturn("data.png");
        when(results.getMapName()).thenReturn("dataMap");
        when(results.getMap()).thenReturn("<map name=\"dataMap\">");
        when(results.getImageFormat()).thenReturn("png");

        MustacheTableDiagram mustacheTableDiagram = new MustacheTableDiagram(
                "Data 1",
                results,
                false
        );
        assertThat(mustacheTableDiagram.getName()).isEqualTo("Data 1");
        assertThat(mustacheTableDiagram.getId()).isEqualTo("data1DegreeImg");
        assertThat(mustacheTableDiagram.getFileName()).isEqualTo("data.png");
        assertThat(mustacheTableDiagram.getMapName()).isEqualTo("dataMap");
        assertThat(mustacheTableDiagram.getMap()).isEqualTo("<map name=\"dataMap\">");
    }
}
