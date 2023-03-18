package org.schemaspy.output.diagram.graphviz;

import com.beust.jcommander.JCommander;
import org.junit.jupiter.api.Test;

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
            parse()
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
            parse()
                .getImageFormat()
        )
            .isEqualTo("png");
    }


    private GraphvizConfig parse(String... args) {
        GraphvizConfigCli graphvizConfigCli = new GraphvizConfigCli();
        JCommander jCommander = JCommander.newBuilder().build();
        jCommander.addObject(graphvizConfigCli);
        jCommander.parse(args);
        return graphvizConfigCli;
    }

}