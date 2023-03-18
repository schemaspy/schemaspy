package org.schemaspy.output.dot.schemaspy;

import com.beust.jcommander.JCommander;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.schemaspy.SimpleRuntimeDotConfig;
import org.schemaspy.cli.NoRowsConfigCli;
import org.schemaspy.cli.TemplateDirectoryConfigCli;
import org.schemaspy.output.dot.DotConfig;
import org.schemaspy.output.dot.DotConfigCli;

class DotConfigHeaderTest {

    /**
     * Given the name of a font,
     * When the object is asked for its header,
     * Then the response should contain the font.
     */
    @Test
    void interpretFonts() {
        MatcherAssert.assertThat(
            new DotConfigHeader(
                new SimpleRuntimeDotConfig(
                    new DefaultFontConfig(parse("-font", "font")),
                    parse("-font", "font"),
                    false,
                    false
                ),
                false
            ).value(),
            CoreMatchers.containsString("    fontname=\"font\"")
        );
    }

    /**
     * Given a request to show labels,
     * When the object is asked for its header,
     * Then the response should justify the label.
     */
    @Test
    void justifyLabel() {
        MatcherAssert.assertThat(
            new DotConfigHeader(
                new SimpleRuntimeDotConfig(
                    new TestFontConfig(),
                    parse(),
                    false,
                    false
                ),
                true
            ).value(),
            CoreMatchers.containsString("    labeljust=\"l\"")
        );
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
