package org.schemaspy;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;

import org.junit.Rule;
import org.junit.Test;
import org.schemaspy.testing.ExitCodeRule;
import org.schemaspy.testing.Logger;
import org.schemaspy.testing.LoggingRule;

public class MainTest {
	@Rule
	public LoggingRule loggingRule = new LoggingRule();

	@Rule
	public ExitCodeRule exitCodeRule = new ExitCodeRule();

	@Test
	@Logger(value = Main.class)
	public void callsSystemExit() {
		try {
			Main.main("-t", "mysql", "-sso", "-o", "target/tmp", "-host", "localhost", "-port", "123154", "-db",
					"qwerty",
					"--logging.config=" + Paths.get("src", "test", "resources", "logback-debugEx.xml").toString());
		} catch (SecurityException ignore) {
		}
		assertThat(exitCodeRule.getExitCode()).isEqualTo(3);
		String log = loggingRule.getLog();
		assertThat(log).contains("StackTraces have been omitted");
	}

}
