package org.schemaspy.app.cli;

import com.beust.jcommander.ParameterException;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class CommandLineArgumentParserTest {

    private static final PropertyFileDefaultProvider NO_DEFAULT_PROVIDER = null;

    @Test
    public void givenNoRequiredParameterProvided_AndNoDefaultProvider_ExpectError() {
        CommandLineArgumentParser parser = new CommandLineArgumentParser(NO_DEFAULT_PROVIDER);

        assertThatThrownBy(parser::parse)
                .isInstanceOf(ParameterException.class)
                .hasMessageContaining("The following options are required:");
    }

    @Test
    public void givenNoRequiredParameterAndDefaultProviderWithoutRequiredValue_ExpectError() {
        PropertyFileDefaultProvider defaultProvider = mock(PropertyFileDefaultProvider.class);
        given(defaultProvider.getDefaultValueFor(any())).willReturn(null);

        CommandLineArgumentParser parser = new CommandLineArgumentParser(defaultProvider);

        assertThatThrownBy(parser::parse)
                .isInstanceOf(ParameterException.class)
                .hasMessageContaining("The following options are required:");
    }

    /**
     * given all required params (o and u) -> expect all ok
     */
    @Test
    public void givenAllRequiredParamsProvided_ExpectToSuccessfullyParseCommandLineArguments() throws Exception {
        Path myOutputDirecotry = Files.createTempDirectory("myOutputDirecotry");

        String[] args = {
                "-o", myOutputDirecotry.toString(),
                "-u", "MyUser"};
        CommandLineArgumentParser parser = new CommandLineArgumentParser(NO_DEFAULT_PROVIDER);

        CommandLineArguments arguments = parser.parse(args);

        assertThat(arguments.getOutputDirectory()).isEqualTo(myOutputDirecotry.toFile());
        assertThat(arguments.getUser()).isEqualTo("MyUser");
    }

    @Test
    public void givenNoRequiredParameterAndDefaultProviderWithRequiredValue_ExpectSuccess() {
        PropertyFileDefaultProvider defaultProvider = mock(PropertyFileDefaultProvider.class);
        given(defaultProvider.getDefaultValueFor("schemaspy.outputDirectory")).willReturn("mydirectory");
        given(defaultProvider.getDefaultValueFor("schemaspy.user")).willReturn("myuser");

        CommandLineArgumentParser parser = new CommandLineArgumentParser(defaultProvider);

        CommandLineArguments commandLineArguments = parser.parse();

        assertThat(commandLineArguments.getOutputDirectory()).isNotNull();
        assertThat(commandLineArguments.getUser()).isNotNull();
    }

    //TODO Implement integration tests (?) for following scenarios, addressing the behavior of ApplicationStartListener.

    // given only parameter -configFile without value -> error

    // given only parameter -configfile=my.properties & my.properties does not exist -> error

    // given only parameter -configfile=my.properties & my.properties exists but missing required params (o,u) -> error

    // given only parameter -configfile=my.properties & my.properties exists and contains required params (o,u) -> ok

    // given one required parameter u and -configFile=my.properties & my.propertier exists and contains required param u -> ok, but take command line parameter value


}