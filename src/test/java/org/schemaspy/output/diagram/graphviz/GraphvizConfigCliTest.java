package org.schemaspy.output.diagram.graphviz;

import org.junit.jupiter.api.Test;
import org.schemaspy.cli.CommandLineArgumentParser;
import org.schemaspy.cli.CommandLineArguments;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class GraphvizConfigCliTest {

    @Test
    void graphvizDir() {
        assertThat(
            parse("-gv", "somePath")
                .getGraphvizDir()
        )
            .isEqualTo("somePath");
    }

    @Test
    void renderer() {
        assertThat(
            parse("-renderer", "someRenderer")
                .getRenderer()
        )
            .isEqualTo("someRenderer");
    }

    @Test
    void lowQuality() {
        assertThat(
            parse("-lq")
                .isLowQuality()
        ).isTrue();
    }

    @Test
    void lowQualityDefault() {
        assertThat(
            parse("")
                .isLowQuality()
        ).isFalse();
    }

    @Test
    void imageFormat() {
        assertThat(
            parse("-imageformat", "svg")
                .getImageFormat()
        )
            .isEqualTo("svg");
    }

    @Test
    void imageFormatDefault() {
        assertThat(
            parse("")
                .getImageFormat()
        )
            .isEqualTo("png");
    }


    private GraphvizConfig parse(String... args) {
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
            .getGraphVizConfig();
    }

}