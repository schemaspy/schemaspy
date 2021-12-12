package org.schemaspy.integrationtesting;

import org.schemaspy.cli.CommandLineArgumentParser;
import org.schemaspy.cli.CommandLineArguments;

public final class Arguments {
	private Arguments() {
		// not to be instantiated
	}

	public static CommandLineArguments parseArguments(String... args) {
		final CommandLineArgumentParser commandLineArgumentParser = new CommandLineArgumentParser(
				new CommandLineArguments());
		return commandLineArgumentParser.parse(args);
	}
}
