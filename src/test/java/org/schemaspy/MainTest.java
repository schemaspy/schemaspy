package org.schemaspy;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.schemaspy.cli.SchemaSpyRunner;
import org.schemaspy.testing.ExitCodeRule;
import org.schemaspy.testing.ResettingOutputCapture;

public class MainTest {

	@Rule
	public ResettingOutputCapture resettingOutputCapture = new ResettingOutputCapture();

	@Rule
	public ExitCodeRule exitCodeRule = new ExitCodeRule();

	@Test
	public void callsSystemExit() {
		resettingOutputCapture.expect(Matchers.containsString("StackTraces have been omitted"));
		try {
			Main.main("-t", "mysql", "-sso", "-o", "target/tmp", "-host", "localhost", "-port", "123154", "-db",
					"qwerty", "-Dlogback.configurationFile="
							+ Paths.get("src", "test", "resources", "logback-debugEx.xml").toString());
		} catch (SecurityException ignore) {
		}
		assertThat(exitCodeRule.getExitCode()).isEqualTo(SchemaSpyRunner.ExitCode.CONNECTION_ERROR.ordinal());
	}

}
