package org.schemaspy.output.html.mustache;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.schemaspy.output.diagram.DiagramException;
import org.schemaspy.output.diagram.DiagramProducer;
import org.schemaspy.output.dot.schemaspy.graph.Graph;
import org.schemaspy.output.html.HtmlException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GraphDiagramTest {

    @TempDir
    private Path outputDir;

    @Test
    void createDiagramElementForAGraph() {
        Graph graph = () -> "this is  a graph";
        DiagramProducer renderer = mock(DiagramProducer.class);
        when(renderer.generateDiagram(any(File.class), any(File.class))).thenReturn("");
        when(renderer.getDiagramFormat()).thenReturn("svg");

        assertThat(new GraphDiagram(graph, renderer, outputDir, "agraph")).isInstanceOf(DiagramElement.class);
    }

    @Test
    void wrapAsHtmlExceptionOnExceptionWithDot() throws IOException {
        File dotFile = outputDir.resolve("agraph.dot").toFile();
        dotFile.createNewFile();
        dotFile.setWritable(false);
        DiagramProducer renderer = mock(DiagramProducer.class);
        GraphDiagram orphanDiagram = new GraphDiagram(() -> "Mocked", renderer, outputDir, "agraph");

        assertThatThrownBy(() -> orphanDiagram.html())
            .isInstanceOf(HtmlException.class);
        verify(renderer, never()).generateDiagram(any(), any());
    }

    @Test
    void wrapAsHtmlExceptionOnExceptionWithDiagram() {
        DiagramProducer renderer = mock(DiagramProducer.class);
        when(renderer.generateDiagram(any(), any())).thenThrow(new DiagramException("Mocked", null));
        GraphDiagram orphanDiagram = new GraphDiagram(() -> "test", renderer, outputDir, "agraph");

        assertThatThrownBy(() -> orphanDiagram.html())
            .isInstanceOf(HtmlException.class);
    }
}