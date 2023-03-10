package org.schemaspy.output.diagram.graphviz;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(resourceBundle = "graphvizconfigcli")
public class GraphvizConfigCli implements GraphvizConfig {

    @Parameter(
            names = {
                    "-gv", "--graphviz-dir",
                    "schemaspy.gv"
            },
            descriptionKey = "gv"
    )
    private String graphvizDir = null;

    @Parameter(
            names = {
                    "-renderer", "--renderer",
                    "schemaspy.renderer"
            },
            descriptionKey = "renderer"
    )
    private String renderer = null;

    @Parameter(
            names = {
                    "-lq", "--low-quality",
                    "schemaspy.lq"
            },
            descriptionKey = "lq"
    )
    private boolean lowQuality = false;

    @Parameter(
            names = {
                    "-imageformat", "--image-format",
                    "schemaspy.imageformat"
            },
            descriptionKey = "imageformat"
    )
    private String imageFormat = "png";

    @Override
    public String getGraphvizDir() {
        return graphvizDir;
    }

    @Override
    public String getRenderer() {
        return renderer;
    }

    @Override
    public boolean isLowQuality() {
        return lowQuality;
    }

    @Override
    public String getImageFormat() {
        return imageFormat;
    }
}
