package org.schemaspy.output.dot;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.schemaspy.cli.NoRowsConfigCli;
import org.schemaspy.cli.TemplateDirectoryConfigCli;

@Parameters(resourceBundle = "dotconfigcli")
public class DotConfigCli implements DotConfig {

    @Parameter(
        names = {
            "-font",
            "schemaspy.font"
        },
        descriptionKey = "font"
    )
    private String font = "Helvetica";

    @Parameter(
        names = {
            "-fontsize", "--font-size",
            "schemspy.fontsize", "schemaspy.font-size"
        },
        descriptionKey = "fontSize"
    )
    private int fontSize = 11;

    @Parameter(
        names = {
            "-rankdirbug",
            "schemaspy.rankdirbug"
        },
        descriptionKey = "rankdirbug"
    )
    private boolean rankDirBugEnabled = false;

    @Parameter(
        names = {
            "-css",
            "schemaspy.css"
        },
        descriptionKey = "css"
    )
    private String css = "schemaSpy.css";

    private NoRowsConfigCli noRowsConfigCli;
    private TemplateDirectoryConfigCli templateDirectoryConfigCli;

    public DotConfigCli(NoRowsConfigCli noRowsConfigCli, TemplateDirectoryConfigCli templateDirectoryConfigCli) {
        this.noRowsConfigCli = noRowsConfigCli;
        this.templateDirectoryConfigCli = templateDirectoryConfigCli;
    }

    @Override
    public String getFont() {
        return font;
    }

    @Override
    public int getFontSize() {
        return fontSize;
    }

    @Override
    public boolean isRankDirBugEnabled() {
        return rankDirBugEnabled;
    }

    @Override
    public String getCss() {
        return css;
    }

    @Override
    public String getTemplateDirectory() {
        return templateDirectoryConfigCli.getTemplateDirectory();
    }

    @Override
    public boolean isNumRowsEnabled() {
        return noRowsConfigCli.isNumRowsEnabled();
    }
}
