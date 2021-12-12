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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.junit.Rule;
import org.junit.Test;
import org.schemaspy.testing.Logger;
import org.schemaspy.testing.LoggingRule;

import com.beust.jcommander.ParameterException;

public class CommandLineArgumentParserTest {

	private static final PropertyFileDefaultProvider NO_DEFAULT_PROVIDER = null;

	@Rule
	public LoggingRule loggingRule = new LoggingRule();

	@Test
	public void givenNoRequiredParameterProvided_AndNoDefaultProvider_ExpectError() {
		CommandLineArgumentParser parser = new CommandLineArgumentParser(new CommandLineArguments(),
				(args) -> NO_DEFAULT_PROVIDER);

		assertThatThrownBy(parser::parse).isInstanceOf(ParameterException.class)
				.hasMessageContaining("The following options are required:");
	}

	@Test
	public void givenNoRequiredParameterAndDefaultProviderWithoutRequiredValue_ExpectError() {
		PropertyFileDefaultProvider defaultProvider = mock(PropertyFileDefaultProvider.class);
		given(defaultProvider.getDefaultValueFor(any())).willReturn(null);

		CommandLineArgumentParser parser = new CommandLineArgumentParser(new CommandLineArguments(),
				(args) -> defaultProvider);

		assertThatThrownBy(parser::parse).isInstanceOf(ParameterException.class)
				.hasMessageContaining("The following options are required:");
	}

	/**
	 * given all required params (o and u) -> expect all ok
	 */
	@Test
	public void givenAllRequiredParamsProvided_ExpectToSuccessfullyParseCommandLineArguments() throws Exception {
		String[] lArgs = { "-o", "aFolder", "-u", "MyUser" };
		CommandLineArgumentParser parser = new CommandLineArgumentParser(new CommandLineArguments(),
				(args) -> NO_DEFAULT_PROVIDER);

		CommandLineArguments arguments = parser.parse(lArgs);

		assertThat(arguments.getOutputDirectory().getPath()).isEqualTo("aFolder");
		assertThat(arguments.getUser()).isEqualTo("MyUser");
	}

	@Test
	public void givenNoRequiredParameterAndDefaultProviderWithRequiredValue_ExpectSuccess() {
		PropertyFileDefaultProvider defaultProvider = mock(PropertyFileDefaultProvider.class);
		given(defaultProvider.getDefaultValueFor("schemaspy.outputDirectory")).willReturn("mydirectory");
		given(defaultProvider.getDefaultValueFor("schemaspy.user")).willReturn("myuser");

		CommandLineArgumentParser parser = new CommandLineArgumentParser(new CommandLineArguments(),
				(args) -> defaultProvider);

		CommandLineArguments commandLineArguments = parser.parse();

		assertThat(commandLineArguments.getOutputDirectory()).isNotNull();
		assertThat(commandLineArguments.getUser()).isNotNull();
	}

	@Test
	public void ssoIsEnabledOnCommandLineUserIsNotRequired() {
		String[] lArgs = { "-o", "aFolder", "-sso" };
		CommandLineArgumentParser parser = new CommandLineArgumentParser(new CommandLineArguments(),
				(args) -> NO_DEFAULT_PROVIDER);

		CommandLineArguments arguments = parser.parse(lArgs);

		assertThat(arguments.getOutputDirectory().getPath()).isEqualTo("aFolder");
		assertThat(arguments.getUser()).isNull();
	}

	@Test
	public void ssoIsEnabledInPropertiesFileUserIsNotRequired() {
		PropertyFileDefaultProvider defaultProvider = mock(PropertyFileDefaultProvider.class);
		given(defaultProvider.getDefaultValueFor("schemaspy.outputDirectory")).willReturn("mydirectory");
		given(defaultProvider.getDefaultValueFor("schemaspy.sso")).willReturn(Boolean.TRUE.toString());

		CommandLineArgumentParser parser = new CommandLineArgumentParser(new CommandLineArguments(),
				(args) -> defaultProvider);

		CommandLineArguments commandLineArguments = parser.parse();

		assertThat(commandLineArguments.getOutputDirectory()).isNotNull();
		assertThat(commandLineArguments.getUser()).isNull();
	}

	@Test
	@Logger(CommandLineArgumentParser.class)
	public void printUsage() {
		String[] lArgs = { "-o", "aFolder", "-sso" };
		CommandLineArgumentParser parser = new CommandLineArgumentParser(new CommandLineArguments(),
				(args) -> NO_DEFAULT_PROVIDER);

		CommandLineArguments arguments = parser.parse(lArgs);
		parser.printUsage();
		assertThat(loggingRule.getLog()).contains("Options:");
	}

	@Test
	public void onlyHelpSetsHelpRequiredShowsHelp() {
		String[] lArgs = { "-help" };
		CommandLineArgumentParser parser = new CommandLineArgumentParser(new CommandLineArguments(),
				(args) -> NO_DEFAULT_PROVIDER);
		CommandLineArguments arguments = parser.parse(lArgs);
		assertThat(arguments.isHelpRequired()).isTrue();
	}

	@Test
	public void onlyDBHelpSetsDBHelpRequired() {
		String[] lArgs = { "-dbHelp" };
		CommandLineArgumentParser parser = new CommandLineArgumentParser(new CommandLineArguments(),
				(args) -> NO_DEFAULT_PROVIDER);
		CommandLineArguments arguments = parser.parse(lArgs);
		assertThat(arguments.isDbHelpRequired()).isTrue();
	}

	@Test
	public void printLicenseFlagWorks() {
		String[] lArgs = { "--license" };
		CommandLineArgumentParser parser = new CommandLineArgumentParser(new CommandLineArguments(),
				(args) -> NO_DEFAULT_PROVIDER);
		CommandLineArguments arguments = parser.parse(lArgs);
		assertThat(arguments.isPrintLicense()).isTrue();
	}

	@Test
	@Logger(CommandLineArgumentParser.class)
	public void canPrintLicense() {
		CommandLineArgumentParser parser = new CommandLineArgumentParser(new CommandLineArguments(),
				(args) -> NO_DEFAULT_PROVIDER);
		parser.printLicense();
		String log = loggingRule.getLog();
		assertThat(log).contains("GNU GENERAL PUBLIC LICENSE");
		assertThat(log).contains("GNU LESSER GENERAL PUBLIC LICENSE");
	}

	@Test
	public void skipHtmlIsFalseByDefault() {
		String[] lArgs = { "-o", "aFolder", "-sso" };
		CommandLineArgumentParser parser = new CommandLineArgumentParser(new CommandLineArguments(),
				(args) -> NO_DEFAULT_PROVIDER);
		CommandLineArguments arguments = parser.parse(lArgs);
		assertThat(arguments.isHtmlDisabled()).isFalse();
		assertThat(arguments.isHtmlEnabled()).isTrue();
	}

	@Test
	public void skipHtmlCanBeEnabled() {
		String[] lArgs = { "-o", "aFolder", "-sso", "-nohtml" };
		CommandLineArgumentParser parser = new CommandLineArgumentParser(new CommandLineArguments(),
				(args) -> NO_DEFAULT_PROVIDER);
		CommandLineArguments arguments = parser.parse(lArgs);
		assertThat(arguments.isHtmlDisabled()).isTrue();
		assertThat(arguments.isHtmlEnabled()).isFalse();
	}

	@Test
	public void degreeOfSeparationIsTwoByDefault() {
		String[] lArgs = { "-o", "aFolder", "-sso" };
		CommandLineArgumentParser parser = new CommandLineArgumentParser(new CommandLineArguments(),
				(args) -> NO_DEFAULT_PROVIDER);
		CommandLineArguments arguments = parser.parse(lArgs);
		assertThat(arguments.getDegreeOfSeparation()).isEqualTo(2);
	}

	@Test
	public void degreeOfSeparationCanBeSetToOne() {
		String[] lArgs = { "-o", "aFolder", "-sso", "-degree", "1" };
		CommandLineArgumentParser parser = new CommandLineArgumentParser(new CommandLineArguments(),
				(args) -> NO_DEFAULT_PROVIDER);
		CommandLineArguments arguments = parser.parse(lArgs);
		assertThat(arguments.getDegreeOfSeparation()).isEqualTo(1);
	}

	@Test
	public void degreeOfSeparationThreeThrowsException() {
		String[] lArgs = { "-o", "aFolder", "-sso", "-degree", "3" };
		CommandLineArgumentParser parser = new CommandLineArgumentParser(new CommandLineArguments(),
				(args) -> NO_DEFAULT_PROVIDER);
		assertThatExceptionOfType(ParameterException.class).isThrownBy(() -> parser.parse(lArgs));
	}

	// TODO Implement integration tests (?) for following scenarios, addressing the
	// behavior of ApplicationStartListener.

	// given only parameter -configFile without value -> error

	// given only parameter -configfile=my.properties & my.properties does not exist
	// -> error

	// given only parameter -configfile=my.properties & my.properties exists but
	// missing required params (o,u) -> error

	// given only parameter -configfile=my.properties & my.properties exists and
	// contains required params (o,u) -> ok

	// given one required parameter u and -configFile=my.properties & my.propertier
	// exists and contains required param u -> ok, but take command line parameter
	// value

}