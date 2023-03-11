package org.schemaspy.output.dot;

import org.junit.jupiter.api.Test;
import org.schemaspy.cli.CommandLineArgumentParser;
import org.schemaspy.cli.CommandLineArguments;

import java.util.Arrays;
import java.util.stream.Stream;

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
            parse("")
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
            parse("")
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
            parse("")
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
            parse("")
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
            parse("")
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
        parse("")
            .isNumRowsEnabled()
        ).isTrue();
    }

    private DotConfig parse(String... args) {
        String[] defaultArgs = {"-o", "out", "-sso"};
        return new CommandLineArgumentParser(
            new CommandLineArguments(),
            (option) -> null
        )
            .parse(
                Stream
                    .concat(
                        Arrays.stream(defaultArgs),
                        Arrays.stream(args)
                    ).toArray(String[]::new))
            .getDotConfig();
    }
}