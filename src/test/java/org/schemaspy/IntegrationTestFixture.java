package org.schemaspy;

import java.util.Optional;

import org.schemaspy.cli.CommandLineArgumentParser;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.cli.ConfigFileArgumentParser;
import org.schemaspy.cli.DefaultProviderFactory;
import org.schemaspy.input.dbms.service.SqlService;

import com.beust.jcommander.IDefaultProvider;

public class IntegrationTestFixture {
	private final SqlService sqlService = new SqlService();
	private final CommandLineArgumentParser commandLineArgumentParser;
	private final CommandLineArguments commandLineArguments;
	private final ConfigFileArgumentParser configFileArgumentParser = new ConfigFileArgumentParser();
	private final DefaultProviderFactory defaultProviderFactory = new DefaultProviderFactory();

	private IntegrationTestFixture(String[] args) {
		Optional<String> configFileName = configFileArgumentParser.parseConfigFileArgumentValue(args);
		final IDefaultProvider iDefaultProvider = defaultProviderFactory.create(configFileName.orElse(null));
		commandLineArgumentParser = new CommandLineArgumentParser(new CommandLineArguments(), iDefaultProvider);
		commandLineArguments = commandLineArgumentParser.parse(args);
	}

	public SqlService sqlService() {
		return sqlService;
	}

	public CommandLineArguments commandLineArguments() {
		return commandLineArguments;
	}

	public CommandLineArgumentParser commandLineArgumentParser() {
		return commandLineArgumentParser;
	}

	public static IntegrationTestFixture fromArgs(String[] args) {
		return new IntegrationTestFixture(args);
	}
}
