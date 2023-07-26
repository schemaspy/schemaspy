package org.schemaspy;

import org.junit.jupiter.api.Test;
import org.schemaspy.cli.CommandLineArgumentParser;
import org.schemaspy.model.Table;
import org.schemaspy.output.dot.DotConfig;
import org.schemaspy.output.dot.RuntimeDotConfig;
import org.schemaspy.output.dot.schemaspy.DefaultFontConfig;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SimpleRuntimeDotConfigTest {

    @Test
    void showDetails() {
        assertThat(
            parse("-maxdet", "600")
                .showDetails(
                    Stream
                        .generate(
                            () -> mock(Table.class)
                        )
                        .limit(500)
                        .collect(Collectors.toList()))
        )
            .isTrue();
    }

    @Test
    void showDetailsDefault() {
        assertThat(
            parse("")
                .showDetails(Stream
                    .generate(
                        () -> mock(Table.class)
                    )
                    .limit(500)
                    .collect(Collectors.toList()))
        )
            .isFalse();
    }

    private RuntimeDotConfig parse(String... args) {
        String[] defaultArgs = {"-o", "out", "-sso"};
        DotConfig dotConfig = new CommandLineArgumentParser(
                Stream
                        .concat(
                                Arrays.stream(defaultArgs),
                                Arrays.stream(args)
                        )
                        .toArray(String[]::new)
        )
            .commandLineArguments()
            .getDotConfig();
        return new SimpleRuntimeDotConfig(
            new DefaultFontConfig(dotConfig),
            dotConfig,
            true,
            true
        );
    }
}