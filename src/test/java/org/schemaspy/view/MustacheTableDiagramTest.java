package org.schemaspy.view;

import org.junit.Test;
import org.schemaspy.output.diagram.DiagramResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MustacheTableDiagramTest {

    @Test
    public void translateDiagramResults() {
        DiagramResult results = mock(DiagramResult.class);
        when(results.getFileName()).thenReturn("orphan.png");
        when(results.getMapName()).thenReturn("orphanMap");
        when(results.getMap()).thenReturn("<map name=\"orphanMap\">");
        when(results.getImageFormat()).thenReturn("png");

        MustacheTableDiagram mustacheTableDiagram = new MustacheTableDiagram(
                "Orphan 1",
                results,
                false
        );
        assertThat(mustacheTableDiagram.getName()).isEqualTo("Orphan 1");
        assertThat(mustacheTableDiagram.getId()).isEqualTo("orphan1DegreeImg");
        assertThat(mustacheTableDiagram.getFileName()).isEqualTo("orphan.png");
        assertThat(mustacheTableDiagram.getMapName()).isEqualTo("orphanMap");
        assertThat(mustacheTableDiagram.getMap()).isEqualTo("<map name=\"orphanMap\">");
    }
}
