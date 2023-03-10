package org.schemaspy.view;

import org.junit.jupiter.api.Test;
import org.schemaspy.cli.CommandLineArgumentParser;
import org.schemaspy.cli.CommandLineArguments;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlConfigCliTest {

    @Test
    void description() {
        assertThat(
            parse("-desc", "MyDatabase")
                .getDescription()
        ).isEqualTo("MyDatabase");
    }

    @Test
    void template() {
        assertThat(
            parse("-template", "override")
                .getTemplateDirectory()
        ).isEqualTo("override");
    }

    @Test
    void templateDefault() {
        assertThat(
            parse("")
                .getTemplateDirectory()
        ).isEqualTo("layout");
    }

    @Test
    void noPages() {
        assertThat(
            parse("-nopages")
                .isPaginationEnabled()
        ).isFalse();
    }

    @Test
    void noPagesDefault() {
        assertThat(
            parse("")
                .isPaginationEnabled()
        ).isTrue();
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

    private HtmlConfig parse(String... args) {
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
            .getHtmlConfig();
    }

}