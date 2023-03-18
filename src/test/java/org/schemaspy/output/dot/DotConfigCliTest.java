package org.schemaspy.output.dot;

import com.beust.jcommander.JCommander;
import org.junit.jupiter.api.Test;
import org.schemaspy.cli.NoRowsConfigCli;
import org.schemaspy.cli.TemplateDirectoryConfigCli;

import static org.assertj.core.api.Assertions.assertThat;

class DotConfigCliTest {

    @Test
    void font() {
        assertThat(
            parse("-font", "Comic Sans")
                .getFont()
        )
            .isEqualTo("Comic Sans");
    }

    void fontDefault() {
        assertThat(
            parse()
                .getFont()
        )
            .isEqualTo("Helvetica");
    }

    @Test
    void fontSize() {
        assertThat(
            parse("-fontsize", "30")
                .getFontSize()
        ).isEqualTo(30);
    }

    @Test
    void fontSizeDefault() {
        assertThat(
            parse()
                .getFontSize()
        ).isEqualTo(11);
    }

    @Test
    void isRankDirBugEnabled() {
        assertThat(
            parse("-rankdirbug")
                .isRankDirBugEnabled()
        )
            .isTrue();
    }

    @Test
    void isRankDirBugEnabledDefault() {
        assertThat(
            parse()
                .isRankDirBugEnabled()
        )
            .isFalse();
    }

    @Test
    void css() {
        assertThat(
            parse("-css", "myStyleSheet.css")
                .getCss()
        )
            .isEqualTo("myStyleSheet.css");
    }

    @Test
    void cssDefault() {
        assertThat(
            parse()
                .getCss()
        )
            .isEqualTo("schemaSpy.css");
    }

    @Test
    void getTemplateDirectory() {
        assertThat(
            parse("-template", "myTemplates")
                .getTemplateDirectory()
        )
            .isEqualTo("myTemplates");
    }

    @Test
    void getTemplateDirectoryDefault() {
        assertThat(
            parse()
                .getTemplateDirectory()
        )
            .isEqualTo("layout");
    }

    @Test
    void noRows() {
        assertThat(
        parse("-norows")
            .isNumRowsEnabled()
        ).isFalse();
    }

    @Test
    void noRowsDefault() {
        assertThat(
        parse()
            .isNumRowsEnabled()
        ).isTrue();
    }

    @Test
    void maxDetails() {
        assertThat(
            parse("-maxdet", "500")
                .getMaxDetailedTables()
        )
            .isEqualTo(500);
    }

    @Test
    void maxDetailsDefault() {
        assertThat(
            parse()
                .getMaxDetailedTables()
        )
            .isEqualTo(300);
    }

    private DotConfig parse(String... args) {
        NoRowsConfigCli noRowsConfigCli = new NoRowsConfigCli();
        TemplateDirectoryConfigCli templateDirectoryConfigCli = new TemplateDirectoryConfigCli();
        DotConfigCli dotConfigCli = new DotConfigCli(noRowsConfigCli, templateDirectoryConfigCli);
        JCommander jCommander = JCommander.newBuilder().build();
        jCommander.addObject(noRowsConfigCli);
        jCommander.addObject(templateDirectoryConfigCli);
        jCommander.addObject(dotConfigCli);
        jCommander.parse(args);
        return dotConfigCli;
    }
}