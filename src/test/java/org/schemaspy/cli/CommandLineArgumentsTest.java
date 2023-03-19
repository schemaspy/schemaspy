package org.schemaspy.cli;

import com.beust.jcommander.JCommander;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommandLineArgumentsTest {

    @Test
    void schemas() {
        assertThat(
            parse("-schemas", "a1,a2")
                .getSchemas()
        )
            .containsExactlyInAnyOrder("a1", "a2");
    }
    @Test
    void schemasRetainsSingleQuot() {
        assertThat(
            parse("-schemas", "'a 1','a 2'")
                .getSchemas()
        )
            .containsExactlyInAnyOrder("'a 1'", "'a 2'");
    }

    @Test
    void schemasWithSpaces() {
        assertThat(
            parse("-schemas", "a 1,a 2")
                .getSchemas()
        )
            .containsExactlyInAnyOrder("a 1", "a 2");
    }

    @Test
    void schemasDefault() {
        assertThat(
            parse()
                .getSchemas()
        )
            .isEmpty();
    }

    private CommandLineArguments parse(String...args) {
        CommandLineArguments commandLineArguments = new CommandLineArguments();
        JCommander jCommander = JCommander.newBuilder().build();
        jCommander.addObject(commandLineArguments);
        jCommander.parse(args);
        return commandLineArguments;
    }

}