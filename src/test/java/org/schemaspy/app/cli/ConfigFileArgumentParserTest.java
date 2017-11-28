package org.schemaspy.app.cli;

import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigFileArgumentParserTest {

    private ConfigFileArgumentParser parser;

    @Before
    public void init() {
        parser = new ConfigFileArgumentParser();
    }

    @Test
    public void givenConfigFileArguemtn_ExpectToParseItsValue() {
        Optional<String> value = parser.parseConfigFileArgumentValue("-configFile", "my.properties");
        assertThat(value).contains("my.properties");
    }

    @Test
    public void givenNoConfigFileArgument_ExpectEmptyConfigFileValue() {
        Optional<String> value = parser.parseConfigFileArgumentValue();
        assertThat(value).isEmpty();
    }
}