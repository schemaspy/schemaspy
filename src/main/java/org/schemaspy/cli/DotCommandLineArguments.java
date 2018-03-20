package org.schemaspy.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.io.File;

@Parameters(resourceBundle = "dotcommandlinearguments")
public class DotCommandLineArguments {

    @Parameter(
            names = {
                    "-gv",
                    "schemaspy.gv"
            },
            descriptionKey = "graphvizDir"
    )
    private File graphvizDir;
    @Parameter(
            names = {
                    "-imageformat",
                    "schemaspy.imageformat"
            },
            descriptionKey = "imageFormat"
    )
    private String imageFormat = "png";

    @Parameter(
            names = {
                    "-hq",
                    "schemaspy.hq"
            },
            descriptionKey = "highQuality"
    )
    private boolean highQuality;
    @Parameter(
            names = {
                    "-lq",
                    "schemaspy.lq"
            },
            descriptionKey = "lowQuality"
    )
    private boolean lowQuality;
    @Parameter(
            names = {
                    "-renderer",
                    "schemaspy.renderer"
            },
            descriptionKey = "renderer"
    )
    private String renderer;

    public File getGraphvizDir() {
        return graphvizDir;
    }

    public String getImageFormat() {
        return imageFormat;
    }

    public boolean isHighQuality() {
        return highQuality;
    }

    public boolean isLowQuality() {
        return lowQuality;
    }

    public String getRenderer() {
        return renderer;
    }
}
