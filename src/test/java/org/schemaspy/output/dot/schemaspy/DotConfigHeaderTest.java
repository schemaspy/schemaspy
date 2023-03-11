package org.schemaspy.output.dot.schemaspy;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.schemaspy.SimpleRuntimeDotConfig;
import org.schemaspy.cli.CommandLineArgumentParser;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.output.dot.DotConfig;

import java.util.Arrays;
import java.util.stream.Stream;

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
                    parse(""),
                    false,
                    false
                ),
                true
            ).value(),
            CoreMatchers.containsString("    labeljust=\"l\"")
        );
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
