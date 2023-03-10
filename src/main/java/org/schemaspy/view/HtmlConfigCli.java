package org.schemaspy.view;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.schemaspy.cli.NoRowsConfigCli;

@Parameters(resourceBundle = "htmlconfigcli")
public class HtmlConfigCli implements HtmlConfig {

    @Parameter(
        names = {
            "-desc", "--description",
            "schemaspy.desc", "schemaspy.description"
        },
        descriptionKey = "desc"
    )
    private String description;

    @Parameter(
        names = {
            "-template",
            "schemaspy.template"
        },
        descriptionKey = "template"
    )
    private String templateDirectory = "layout";

    @Parameter(
        names = {
            "-nopages", "--no-pages",
            "schemaspy.nopages", "schemaspy.no-pages"
        },
        descriptionKey = "nopages"
    )
    private boolean noPages = false;

    private NoRowsConfigCli noRowsConfigCli;

    public HtmlConfigCli(NoRowsConfigCli noRowsConfigCli) {
        this.noRowsConfigCli = noRowsConfigCli;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getTemplateDirectory() {
        return templateDirectory;
    }

    @Override
    public boolean isPaginationEnabled() {
        return !noPages;
    }

    @Override
    public boolean isNumRowsEnabled() {
        return noRowsConfigCli.isNumRowsEnabled();
    }
}
