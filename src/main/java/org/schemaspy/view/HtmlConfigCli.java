package org.schemaspy.view;

import java.util.Collection;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.schemaspy.cli.NoRowsConfigCli;
import org.schemaspy.cli.TemplateDirectoryConfigCli;
import org.schemaspy.model.Table;
import org.schemaspy.util.markup.Asciidoc;
import org.schemaspy.util.markup.Markdown;
import org.schemaspy.util.markup.Markup;
import org.schemaspy.util.markup.PageRegistry;

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

    @Parameter(
        names = {
            "-asciidoc", "--asciidoc",
            "schemaspy.asciidoc"
        },
        descriptionKey = "asciidoc"
    )
    private boolean useAsciiDoc = false;

    private final NoRowsConfigCli noRowsConfigCli;
    private final TemplateDirectoryConfigCli templateDirectoryConfigCli;

    private final PageRegistry pageRegistry = new PageRegistry();

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

    @Override
    public void registryPage(final Collection<Table> tables) {
        pageRegistry.register(tables);
    }

    @Override
    public Markup markupProcessor() {
        if (useAsciiDoc) {
            return new Asciidoc(pageRegistry);
        } else {
            return new Markdown(pageRegistry);
        }
    }
}
