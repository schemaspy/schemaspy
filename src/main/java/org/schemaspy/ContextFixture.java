package org.schemaspy;

import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.cli.ConfigFileArgumentParser;
import org.schemaspy.cli.DefaultProviderFactory;
import org.schemaspy.input.dbms.service.SqlService;

public class ContextFixture {
	private final SqlService sqlService = new SqlService();
	private final CommandLineArguments commandLineArguments = new CommandLineArguments();
	private final ConfigFileArgumentParser configFileArgumentParser = new ConfigFileArgumentParser();
	private final DefaultProviderFactory defaultProviderFactory = new DefaultProviderFactory();

	public SqlService sqlService() {
		return sqlService;
	}

	public CommandLineArguments commandLineArguments() {
		return commandLineArguments;
	}

	public ConfigFileArgumentParser configFileArgumentParser() {
		return configFileArgumentParser;
	}

	public DefaultProviderFactory defaultProviderFactory() {
		return defaultProviderFactory;
	}
}
