package org.schemaspy;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.schemaspy.testing.ExitCodeRule;
import org.springframework.boot.test.system.OutputCaptureRule;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
public class MainTest {

    @Rule
    public OutputCaptureRule resettingOutputCapture = new OutputCaptureRule();

    @Rule
    public ExitCodeRule exitCodeRule = new ExitCodeRule();

    @Test
    public void callsSystemExit() {
        resettingOutputCapture.expect(Matchers.containsString("StackTraces have been omitted"));
        try {
            Main.main(
                    "-t", "mysql",
                    "-sso",
                    "-o", "target/tmp",
                    "-host", "localhost",
                    "-port", "123154",
                    "-db", "qwerty",
                    "--logging.config="+ Paths.get("src","test","resources","logback-debugEx.xml")
            );
        } catch (SecurityException ignore) { }
        assertThat(exitCodeRule.getExitCode()).isEqualTo(3);
    }

    @Test
    public void printUsage() {
        resettingOutputCapture.expect(Matchers.containsString("-u, --user, schemaspy.u, schemaspy.user"));
        try {
            Main.main("-h");
        } catch (SecurityException ignore) { }
        assertThat(exitCodeRule.getExitCode()).isZero();
    }

    @Test
    public void printDbHelp() {
        resettingOutputCapture.expect(Matchers.containsString("You can use your own database types"));
        try {
            Main.main("-dbhelp");
        } catch (SecurityException ignore) { }
        assertThat(exitCodeRule.getExitCode()).isZero();
    }

    @Test
    public void enableDebug() {
        resettingOutputCapture.expect(Matchers.containsString("Debug enabled"));
        try {
            Main.main(
                    "-t", "mysql",
                    "-sso",
                    "-o", "target/tmp",
                    "-host", "localhost",
                    "-port", "123154",
                    "-db", "qwerty",
                    "-debug",
                    "--logging.config="+ Paths.get("src","test","resources","logback-debugEx.xml")
            );
        } catch (SecurityException ignore) { }
        assertThat(exitCodeRule.getExitCode()).isEqualTo(3);
    }

    @Test
    public void parsingError() {
        try {
            Main.main("-t", "-t", "--logging.config="+ Paths.get("src","test","resources","logback-debugEx.xml"));
        } catch (SecurityException ignore) { }
        assertThat(exitCodeRule.getExitCode()).isOne();
    }

}
