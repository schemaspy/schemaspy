/*
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
package org.schemaspy.integrationtesting;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.schemaspy.cli.SchemaSpyRunner;
import org.schemaspy.testing.Logger;
import org.schemaspy.testing.LoggingRule;

/**
 * @author Nils Petzaell
 */
public class StackTraceOmitterIT {

	@Rule
	public LoggingRule loggingRule = new LoggingRule();

	private SchemaSpyRunner schemaSpyRunner = new SchemaSpyRunner();

	@Test
	@Logger(value = SchemaSpyRunner.class, pattern = "%msg%n%debugEx")
	public void noStacktraceWhenLoggingIsOf() {
		schemaSpyRunner.run(new String[] { "-sso", "-o", "target/somefolder", "-t", "doesnt-exist" });
		String log = loggingRule.getLog();
		assertThat(log).isNotEmpty();
		assertThat(log).doesNotContain("Caused by:");
	}

	@Test
	@Logger(value = SchemaSpyRunner.class, pattern = "%msg%n%debugEx")
	public void stacktraceWhenLoggingIsOn() {
		schemaSpyRunner.run(new String[] { "-sso", "-o", "target/somefolder", "-t", "doesnt-exist", "-debug" });
		String log = loggingRule.getLog();
		assertThat(log).isNotEmpty();
		assertThat(log).contains("Caused by:");
	}
}
