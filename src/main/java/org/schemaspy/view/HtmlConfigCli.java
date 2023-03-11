package org.schemaspy.view;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.schemaspy.cli.NoRowsConfigCli;
import org.schemaspy.cli.TemplateDirectoryConfigCli;

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
            "-nopages", "--no-pages",
            "schemaspy.nopages", "schemaspy.no-pages"
        },
        descriptionKey = "nopages"
    )
    private boolean noPages = false;

    private NoRowsConfigCli noRowsConfigCli;
    private TemplateDirectoryConfigCli templateDirectoryConfigCli;

    public HtmlConfigCli(
        NoRowsConfigCli noRowsConfigCli,
        TemplateDirectoryConfigCli templateDirectoryConfigCli
    ) {
        this.noRowsConfigCli = noRowsConfigCli;
        this.templateDirectoryConfigCli = templateDirectoryConfigCli;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getTemplateDirectory() {
        return templateDirectoryConfigCli.getTemplateDirectory();
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
