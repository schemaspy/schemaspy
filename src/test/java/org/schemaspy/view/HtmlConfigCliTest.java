package org.schemaspy.view;

import java.util.Arrays;
import java.util.stream.Stream;

import com.beust.jcommander.IDefaultProvider;
import org.junit.jupiter.api.Test;
import org.schemaspy.cli.CombinedDefaultProvider;
import org.schemaspy.cli.CommandLineArgumentParser;
import org.schemaspy.util.markup.Asciidoc;
import org.schemaspy.util.markup.Markdown;

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

    @Test
    void doNotUseAsciiDoc() {
        assertThat(
            parse("")
                .markupProcessor()
        ).isInstanceOf(Markdown.class);
    }

    @Test
    void useAsciiDocArg() {
        assertThat(
            parse("-asciidoc")
                .markupProcessor()
        ).isInstanceOf(Asciidoc.class);
    }

    @Test
    void useAsciiDocProperty() {
        assertThat(
            parse(
                optionName -> optionName.equals("schemaspy.asciidoc") ? "" : null,
                ""
            ).markupProcessor()
        ).isInstanceOf(Asciidoc.class);
    }

    private HtmlConfig parse(String... args) {
        return parse(optionName -> null, args);
    }

    private HtmlConfig parse(IDefaultProvider iDefaultProvider, String...args) {
        String[] defaultArgs = {"-o", "out", "-sso"};
        return new CommandLineArgumentParser(
            new CombinedDefaultProvider(iDefaultProvider),
            Stream
                .concat(
                    Arrays.stream(defaultArgs),
                    Arrays.stream(args)
                ).toArray(String[]::new)
        )
            .commandLineArguments()
            .getHtmlConfig();
    }

}