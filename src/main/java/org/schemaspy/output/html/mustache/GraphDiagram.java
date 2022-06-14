package org.schemaspy.output.html.mustache;

import org.schemaspy.output.diagram.DiagramException;
import org.schemaspy.output.diagram.DiagramProducer;
import org.schemaspy.output.dot.schemaspy.graph.Graph;
import org.schemaspy.output.html.HtmlException;
import org.schemaspy.util.Writers;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;

public class GraphDiagram implements DiagramElement {

    private final Graph graph;
    private final DiagramProducer renderer;
    private final Path outputDir;
    private final String name;

    public GraphDiagram(Graph graph, DiagramProducer renderer, Path outputDir, String name) {
        this.graph = graph;
        this.renderer = renderer;
        this.outputDir = outputDir;
        this.name = name;
    }

    @Override
    public String html() {
        outputDir.toFile().mkdirs();
        return writeDiagram(writeDot()).html();
    }

    private Path writeDot() {
        Path dotFile = outputDir.resolve(fileName("dot"));
        try (PrintWriter dotOut = Writers.newPrintWriter(dotFile.toFile())) {
            dotOut.println(graph.dot());
            dotOut.flush();
        } catch (IOException e) {
            throw new HtmlException("Failed to write dot: " + dotFile, e);
        }
        return dotFile;
    }

    private DiagramElement writeDiagram(Path dotFile) {
        Path diagramFile = outputDir.resolve(fileName(renderer.getDiagramFormat()));
        try {
            String diagramMap = renderer.generateDiagram(dotFile.toFile(), diagramFile.toFile());
            String diagramSource = diagramFile.getFileName().toString();
            if ("svg".equalsIgnoreCase(renderer.getDiagramFormat())) {
                return new SvgDiagram(name, diagramSource);
            }
            return new ImgDiagram(name, diagramSource, diagramMap);
        } catch (DiagramException diagramException) {
            throw new HtmlException("Failed to generate diagram: " + diagramFile, diagramException);
        }
    }

    private String fileName(String extension) {
        return name + "." + extension;
    }
}
