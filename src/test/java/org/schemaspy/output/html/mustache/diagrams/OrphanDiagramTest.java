package org.schemaspy.output.html.mustache.diagrams;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.schemaspy.output.diagram.DiagramException;
import org.schemaspy.output.diagram.DiagramProducer;
import org.schemaspy.output.dot.schemaspy.graph.Graph;
import org.schemaspy.output.html.HtmlException;
import org.schemaspy.output.html.mustache.DiagramElement;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrphanDiagramTest {

    @TempDir
    private File outputDir;

    @Test
    void createDiagramElementForAGraph() {
        Graph graph = () -> "this is  a graph";
        DiagramProducer diagramProducer = mock(DiagramProducer.class);
        when(diagramProducer.generateDiagram(any(File.class), any(File.class))).thenReturn("");
        when(diagramProducer.getDiagramFormat()).thenReturn("svg");

        assertThat(new OrphanDiagram(graph, diagramProducer, outputDir)).isInstanceOf(DiagramElement.class);
    }

    @Test
    void wrapAsHtmlExceptionOnExceptionWithDot() throws IOException {
        Path dotFile = outputDir.toPath()
            .resolve("diagrams")
            .resolve("orphans")
            .resolve("orphans.dot");
        dotFile.getParent().toFile().mkdirs();
        dotFile.toFile().createNewFile();
        dotFile.toFile().setWritable(false);
        DiagramProducer diagramProducer = mock(DiagramProducer.class);
        OrphanDiagram orphanDiagram = new OrphanDiagram(() -> "Mocked", diagramProducer, outputDir);

        assertThatThrownBy(() -> orphanDiagram.html())
                .isInstanceOf(HtmlException.class);
        verify(diagramProducer, never()).generateDiagram(any(), any());
    }

    @Test
    void wrapAsHtmlExceptionOnExceptionWithDiagram() {
        DiagramProducer diagramProducer = mock(DiagramProducer.class);
        when(diagramProducer.generateDiagram(any(), any())).thenThrow(new DiagramException("Mocked", null));
        OrphanDiagram orphanDiagram = new OrphanDiagram(() -> "test", diagramProducer, outputDir);

        assertThatThrownBy(() -> orphanDiagram.html())
                .isInstanceOf(HtmlException.class);
    }
}