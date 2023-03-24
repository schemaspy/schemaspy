package org.schemaspy.cli;

import com.beust.jcommander.IDefaultProvider;
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

    @Test
    void schemasDefaultFromDefaultsProvider() {
        assertThat(
            parse((optionName ->
                optionName.equals("schemaspy.schemas")
                    ? "a 1,a 2"
                    : null)
            )
                .getSchemas()
        )
            .containsExactlyInAnyOrder("a 1", "a 2");
    }

    private CommandLineArguments parse(String...args) {
        return parse(optionName -> null, args);
    }

    private CommandLineArguments parse(IDefaultProvider iDefaultProvider, String...args) {
        CommandLineArguments commandLineArguments = new CommandLineArguments();
        JCommander jCommander = JCommander
            .newBuilder()
            .defaultProvider(iDefaultProvider)
            .build();
        jCommander.addObject(commandLineArguments);
        jCommander.parse(args);
        return commandLineArguments;
    }

}