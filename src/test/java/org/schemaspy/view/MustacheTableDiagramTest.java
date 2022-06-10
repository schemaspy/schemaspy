package org.schemaspy.view;

import org.junit.Test;
import org.schemaspy.output.diagram.DiagramResults;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MustacheTableDiagramTest {

    @Test
    public void translateDiagramResults() {
        File orphan = mock(File.class);
        when(orphan.getName()).thenReturn("orphan.png");

        DiagramResults results = mock(DiagramResults.class);
        when(results.getDiagramFile()).thenReturn(orphan);
        when(results.getDiagramMapName()).thenReturn("orphanMap");
        when(results.getDiagramMap()).thenReturn("<map name=\"orphanMap\">");
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
