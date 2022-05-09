package org.schemaspy.output.diagram.graphviz;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(resourceBundle = "graphvizconfigcli")
public class GraphvizConfigCli implements GraphvizConfig {

    @Parameter(
            names = {
                    "-gv", "--graphviz-dir", "gv",
                    "schemaspy.gv"
            },
            descriptionKey = "gv"
    )
    private String graphvizDir = null;

    @Parameter(
            names = {
                    "-renderer", "--renderer", "renderer",
                    "schemaspy.renderer"
            },
            descriptionKey = "renderer"
    )
    private String renderer = null;

    @Parameter(
            names = {
                    "-hq", "--high-quality", "hq",
                    "schemaspy.hq"
            },
            descriptionKey = "hq"
    )
    private boolean highQuality = false;

    @Parameter(
            names = {
                    "-imageformat", "--image-format", "imageformat",
                    "schemaspy.imageformat"
            },
            descriptionKey = "imageformat"
    )
    private String imageFormat = "png";

    public GraphvizConfigCli() {}

    private GraphvizConfigCli(String graphvizDir, String renderer, boolean highQuality, String imageFormat) {
        this.graphvizDir = graphvizDir;
        this.renderer = renderer;
        this.highQuality = highQuality;
        this.imageFormat = imageFormat;
    }

    public GraphvizConfigCli withGraphvizDir(String graphvizDir) {
        return new GraphvizConfigCli(graphvizDir, renderer, highQuality, imageFormat);
    }

    public GraphvizConfigCli withRenderer(String renderer) {
        return new GraphvizConfigCli(graphvizDir, renderer, highQuality, imageFormat);
    }

    public GraphvizConfigCli withHighQuality() {
        return new GraphvizConfigCli(graphvizDir, renderer, true, imageFormat);
    }

    public GraphvizConfigCli withImageFormat(String imageFormat) {
        return new GraphvizConfigCli(graphvizDir, renderer, highQuality, imageFormat);
    }

    @Override
    public String getGraphvizDir() {
        return graphvizDir;
    }

    @Override
    public String getRenderer() {
        return renderer;
    }

    @Override
    public boolean isHighQuality() {
        return highQuality;
    }

    @Override
    public String getImageFormat() {
        return imageFormat;
    }
}
