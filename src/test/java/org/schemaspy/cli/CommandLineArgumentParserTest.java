/*
 * Copyright (C) 2017 Thomas Traude
 * Copyright (C) 2018 Nils Petzaell
 *
 * This file is part of SchemaSpy.
 *
 * SchemaSpy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SchemaSpy. If not, see <http://www.gnu.org/licenses/>.
 */
package org.schemaspy.cli;

import com.beust.jcommander.ParameterException;
import org.junit.Rule;
import org.junit.Test;
import org.schemaspy.testing.Logger;
import org.schemaspy.testing.LoggingRule;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class CommandLineArgumentParserTest {

    private static final PropertyFileDefaultProvider NO_DEFAULT_PROVIDER = null;

    @Rule
    public LoggingRule loggingRule = new LoggingRule();

    @Test
    public void givenNoRequiredParameterProvided_AndNoDefaultProvider_ExpectError() {
        CommandLineArgumentParser parser = new CommandLineArgumentParser(new CommandLineArguments(), NO_DEFAULT_PROVIDER);

        assertThatThrownBy(parser::parse)
                .isInstanceOf(ParameterException.class)
                .hasMessageContaining("The following options are required:");
    }

    @Test
    public void givenNoRequiredParameterAndDefaultProviderWithoutRequiredValue_ExpectError() {
        PropertyFileDefaultProvider defaultProvider = mock(PropertyFileDefaultProvider.class);
        given(defaultProvider.getDefaultValueFor(any())).willReturn(null);

        CommandLineArgumentParser parser = new CommandLineArgumentParser(new CommandLineArguments(), defaultProvider);

        assertThatThrownBy(parser::parse)
                .isInstanceOf(ParameterException.class)
                .hasMessageContaining("The following options are required:");
    }

    /**
     * given all required params (o and u) -> expect all ok
     */
    @Test
    public void givenAllRequiredParamsProvided_ExpectToSuccessfullyParseCommandLineArguments() throws Exception {
        String[] args = {
                "-o", "aFolder",
                "-u", "MyUser"
        };
        CommandLineArgumentParser parser = new CommandLineArgumentParser(new CommandLineArguments(), NO_DEFAULT_PROVIDER);

        CommandLineArguments arguments = parser.parse(args);

        assertThat(arguments.getOutputDirectory().getPath()).isEqualTo("aFolder");
    }

    @Test
    public void givenNoRequiredParameterAndDefaultProviderWithRequiredValue_ExpectSuccess() {
        PropertyFileDefaultProvider defaultProvider = mock(PropertyFileDefaultProvider.class);
        given(defaultProvider.getDefaultValueFor("schemaspy.outputDirectory")).willReturn("mydirectory");
        given(defaultProvider.getDefaultValueFor("schemaspy.user")).willReturn("myuser");

        CommandLineArgumentParser parser = new CommandLineArgumentParser(new CommandLineArguments(), defaultProvider);

        CommandLineArguments commandLineArguments = parser.parse();

        assertThat(commandLineArguments.getOutputDirectory()).isNotNull();
    }

    @Test
    public void ssoIsEnabledOnCommandLineUserIsNotRequired() {
        String[] args = {
                "-o", "aFolder",
                "-sso"
        };
        CommandLineArgumentParser parser = new CommandLineArgumentParser(new CommandLineArguments(), NO_DEFAULT_PROVIDER);

        CommandLineArguments arguments = parser.parse(args);

        assertThat(arguments.getOutputDirectory().getPath()).isEqualTo("aFolder");
    }

    @Test
    public void ssoIsEnabledInPropertiesFileUserIsNotRequired() {
        PropertyFileDefaultProvider defaultProvider = mock(PropertyFileDefaultProvider.class);
        given(defaultProvider.getDefaultValueFor("schemaspy.outputDirectory")).willReturn("mydirectory");
        given(defaultProvider.getDefaultValueFor("schemaspy.sso")).willReturn(Boolean.TRUE.toString());

        CommandLineArgumentParser parser = new CommandLineArgumentParser(new CommandLineArguments(), defaultProvider);

        CommandLineArguments commandLineArguments = parser.parse();

        assertThat(commandLineArguments.getOutputDirectory()).isNotNull();
    }

    @Test
    @Logger(CommandLineArgumentParser.class)
    public void printUsage() {
        String[] args = {
                "-o", "aFolder",
                "-sso"
        };
        CommandLineArgumentParser parser = new CommandLineArgumentParser(new CommandLineArguments(), NO_DEFAULT_PROVIDER);

        CommandLineArguments arguments = parser.parse(args);
        parser.printUsage();
        assertThat(loggingRule.getLog()).contains("Options:");
    }

    @Test
    public void onlyHelpSetsHelpRequiredShowsHelp() {
        String[] args = {
                "-help"
        };
        CommandLineArgumentParser parser = new CommandLineArgumentParser(new CommandLineArguments(), NO_DEFAULT_PROVIDER);
        CommandLineArguments arguments = parser.parse(args);
        assertThat(arguments.isHelpRequired()).isTrue();
    }

    @Test
    public void onlyDBHelpSetsDBHelpRequired() {
        String[] args = {
                "-dbHelp"
        };
        CommandLineArgumentParser parser = new CommandLineArgumentParser(new CommandLineArguments(), NO_DEFAULT_PROVIDER);
        CommandLineArguments arguments = parser.parse(args);
        assertThat(arguments.isDbHelpRequired()).isTrue();
    }

    @Test
    public void printLicenseFlagWorks() {
        String[] args = {
                "--license"
        };
        CommandLineArgumentParser parser = new CommandLineArgumentParser(new CommandLineArguments(), NO_DEFAULT_PROVIDER);
        CommandLineArguments arguments = parser.parse(args);
        assertThat(arguments.isPrintLicense()).isTrue();
    }

    @Test
    @Logger(CommandLineArgumentParser.class)
    public void canPrintLicense() {
        CommandLineArgumentParser parser = new CommandLineArgumentParser(new CommandLineArguments(), NO_DEFAULT_PROVIDER);
        parser.printLicense();
        String log = loggingRule.getLog();
        assertThat(log)
            .contains("GNU GENERAL PUBLIC LICENSE")
            .contains("GNU LESSER GENERAL PUBLIC LICENSE");
    }

    @Test
    public void skipHtmlIsFalseByDefault() {
        String[] args = {
                "-o", "aFolder",
                "-sso"
        };
        CommandLineArgumentParser parser = new CommandLineArgumentParser(new CommandLineArguments(),NO_DEFAULT_PROVIDER);
        CommandLineArguments arguments = parser.parse(args);
        assertThat(arguments.isHtmlDisabled()).isFalse();
        assertThat(arguments.isHtmlEnabled()).isTrue();
    }

    @Test
    public void skipHtmlCanBeEnabled() {
        String[] args = {
                "-o", "aFolder",
                "-sso",
                "-nohtml"
        };
        CommandLineArgumentParser parser = new CommandLineArgumentParser(new CommandLineArguments(),NO_DEFAULT_PROVIDER);
        CommandLineArguments arguments = parser.parse(args);
        assertThat(arguments.isHtmlDisabled()).isTrue();
        assertThat(arguments.isHtmlEnabled()).isFalse();
    }

    @Test
    public void degreeOfSeparationIsTwoByDefault() {
        String[] args = {
                "-o", "aFolder",
                "-sso"
        };
        CommandLineArgumentParser parser = new CommandLineArgumentParser(new CommandLineArguments(),NO_DEFAULT_PROVIDER);
        CommandLineArguments arguments = parser.parse(args);
        assertThat(arguments.getDegreeOfSeparation()).isEqualTo(2);
    }

    @Test
    public void degreeOfSeparationCanBeSetToOne() {
        String[] args = {
                "-o", "aFolder",
                "-sso",
                "-degree", "1"
        };
        CommandLineArgumentParser parser = new CommandLineArgumentParser(new CommandLineArguments(),NO_DEFAULT_PROVIDER);
        CommandLineArguments arguments = parser.parse(args);
        assertThat(arguments.getDegreeOfSeparation()).isEqualTo(1);
    }

    @Test
    public void degreeOfSeparationThreeThrowsException() {
        String[] args = {
                "-o", "aFolder",
                "-sso",
                "-degree", "3"
        };
        CommandLineArgumentParser parser = new CommandLineArgumentParser(new CommandLineArguments(),NO_DEFAULT_PROVIDER);
        assertThatExceptionOfType(ParameterException.class)
                .isThrownBy(() ->parser.parse(args));
    }

    @Test
    public void testDataTablesConfig() {
        String[] args = ("-o output/folder/ -u db_user " +
                "-noDbObjectPaging -columnPageLength 45 -columnLengthChange -tablePageLength 20 " +
                "-indexLengthChange -noCheckPaging -routineLengthChange")
                .split(" ");

        CommandLineArgumentParser parser = new CommandLineArgumentParser(
                new CommandLineArguments(), NO_DEFAULT_PROVIDER);
        CommandLineArguments commandLine = parser.parse(args);

        assertThat(commandLine.isNoDbObjectPaging()).isTrue();
        assertThat(commandLine.isNoCheckPaging()).isTrue();
        assertThat(commandLine.isNoIndexPaging()).isFalse();
        assertThat(commandLine.isNoRoutinePaging()).isFalse();

        assertThat(commandLine.getColumnPageLength()).isEqualTo(45);
        assertThat(commandLine.getTablePageLength()).isEqualTo(20);
        assertThat(commandLine.getDbObjectPageLength()).isEqualTo(50);
        assertThat(commandLine.getIndexPageLength()).isEqualTo(10);

        assertThat(commandLine.isIndexLengthChange()).isTrue();
        assertThat(commandLine.isRoutineLengthChange()).isTrue();
        assertThat(commandLine.isFkLengthChange()).isFalse();
        assertThat(commandLine.isDbObjectLengthChange()).isFalse();
    }

    @Test
    public void dontTreatWordAsParameterName() {
        String[] args = {
            "-u", "user",
            "-p", "user",
            "-o", "aFolder",
        };
        CommandLineArgumentParser commandLineArgumentParser = new CommandLineArgumentParser(
            new CommandLineArguments(), NO_DEFAULT_PROVIDER
        );

        assertThatCode(() -> commandLineArgumentParser.parse(args)).doesNotThrowAnyException();
    }

    @Test
    public void unkownOptionsAreStoredInArgument() {
        String[] args = {
            "-o", "aFolder",
            "-sso",
            "-server", "xds"
        };
        CommandLineArguments arguments = new CommandLineArgumentParser(
            new CommandLineArguments(),
            NO_DEFAULT_PROVIDER
        ).parse(args);
        assertThat(arguments.getConnectionConfig().getRemainingArguments()).containsExactly("-server", "xds");
    }

    //TODO Implement integration tests (?) for following scenarios, addressing the behavior of ApplicationStartListener.

    // given only parameter -configFile without value -> error

    // given only parameter -configfile=my.properties & my.properties does not exist -> error

    // given only parameter -configfile=my.properties & my.properties exists but missing required params (o,u) -> error

    // given only parameter -configfile=my.properties & my.properties exists and contains required params (o,u) -> ok

    // given one required parameter u and -configFile=my.properties & my.propertier exists and contains required param u -> ok, but take command line parameter value


}