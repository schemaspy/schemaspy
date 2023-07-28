package org.schemaspy;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.schemaspy.testing.exit.AssertExitCode;
import org.schemaspy.testing.logback.Logback;
import org.schemaspy.testing.logback.LogbackExtension;

public class MainIT {

    @RegisterExtension
    public static LogbackExtension logback = new LogbackExtension();

    @Test
    @AssertExitCode(3)
    public void callsSystemExit() { //NOSONAR AssertionHandled by @AssertExitCode
            Main.main(
                    "-t", "mysql",
                    "-sso",
                    "-o", "target/tmp",
                    "-host", "localhost",
                    "-port", "123154",
                    "-db", "qwerty"
            );
    }

    @Test
    @AssertExitCode
    @Logback(logger = "root")
    public void printUsage() {
        logback.expect(Matchers.containsString("-u, --user, schemaspy.u, schemaspy.user"));
        Main.main("-h");
    }

    @Test
    @AssertExitCode
    @Logback(logger = "root")
    public void printDbHelp() {
        logback.expect(Matchers.containsString("You can use your own database types"));
        Main.main("-dbhelp");
    }

    @Test
    @AssertExitCode(1)
    public void parsingError() { //NOSONAR AssertionHandled by @AssertExitCode
        Main.main("-t", "-t");
    }

    @Test
    @AssertExitCode(4)
    @Logback(logger = "org.schemaspy", pattern = "%msg%n%debugEx")
    public void noStacktraceWhenLoggingIsOf() {
        logback.expect(Matchers.not(Matchers.containsString("Caused by:")));
        Main.main("-sso", "-o", "target/somefolder", "-t", "doesnt-exist");
    }

    @Test
    @AssertExitCode(4)
    @Logback(logger = "org.schemaspy", pattern = "%msg%n%debugEx")
    public void stacktraceWhenLoggingIsOn() {
        logback.expect(Matchers.containsString("Caused by:"));
        Main.main("-sso", "-o", "target/somefolder", "-t", "doesnt-exist", "-debug");
    }

}
