package org.schemaspy.output.html.mustache.diagrams;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.schemaspy.output.diagram.RenderException;
import org.schemaspy.output.diagram.Renderer;
import org.schemaspy.output.dot.schemaspy.graph.Graph;
import org.schemaspy.output.html.HtmlException;
import org.schemaspy.output.html.mustache.Diagram;

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
        Renderer renderer = mock(Renderer.class);
        when(renderer.render(any(File.class), any(File.class))).thenReturn("");
        when(renderer.format()).thenReturn("svg");

        assertThat(new OrphanDiagram(graph, renderer, outputDir)).isInstanceOf(Diagram.class);
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
        Renderer renderer = mock(Renderer.class);
        OrphanDiagram orphanDiagram = new OrphanDiagram(() -> "Mocked", renderer, outputDir);

        assertThatThrownBy(() -> orphanDiagram.html())
                .isInstanceOf(HtmlException.class);
        verify(renderer, never()).render(any(), any());
    }

    @Test
    void wrapAsHtmlExceptionOnExceptionWithDiagram() {
        Renderer renderer = mock(Renderer.class);
        when(renderer.render(any(), any())).thenThrow(new RenderException("Mocked", null));
        OrphanDiagram orphanDiagram = new OrphanDiagram(() -> "test", renderer, outputDir);

        assertThatThrownBy(() -> orphanDiagram.html())
                .isInstanceOf(HtmlException.class);
    }
}