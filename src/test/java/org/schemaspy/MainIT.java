package org.schemaspy;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.schemaspy.testing.exit.AssertExitCode;
import org.schemaspy.testing.logback.Logback;
import org.schemaspy.testing.logback.LogbackExtension;

class MainIT {

    @RegisterExtension
    static LogbackExtension logback = new LogbackExtension();

    @Test
    @AssertExitCode(3)
    void callsSystemExit() { //NOSONAR AssertionHandled by @AssertExitCode
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
    @Logback(logger = "org.schemaspy.cli.CommandLineArgumentParser")
    void printUsage() {
        logback.expect(Matchers.containsString("-u, --user, schemaspy.u, schemaspy.user"));
        Main.main("-h");
    }

    @Test
    @AssertExitCode
    @Logback(logger = "org.schemaspy.cli.CommandLineArgumentParser")
    void printDbHelp() {
        logback.expect(Matchers.containsString("You can use your own database types"));
        Main.main("-dbhelp");
    }

    @Test
    @AssertExitCode
    @Logback(logger = "org.schemaspy.cli.CommandLineArgumentParser")
    void printLicense() {
        logback.expect(Matchers.containsString("GNU GENERAL PUBLIC LICENSE"));
        logback.expect(Matchers.containsString("GNU LESSER GENERAL PUBLIC LICENSE"));
        Main.main("-l");
    }

    @Test
    @AssertExitCode(1)
    @SuppressWarnings("squid:S2699")
    void parsingError() {
        Main.main("-t", "-t");
    }

    @Test
    @AssertExitCode(4)
    @Logback(logger = "org.schemaspy", pattern = "%msg%n%debugEx")
    void noStacktraceWhenLoggingIsOf() {
        logback.expect(Matchers.not(Matchers.containsString("Caused by:")));
        Main.main("-sso", "-o", "target/somefolder", "-t", "doesnt-exist");
    }

    @Test
    @AssertExitCode(4)
    @Logback(logger = "org.schemaspy", pattern = "%msg%n%debugEx")
    void stacktraceWhenLoggingIsOn() {
        logback.expect(Matchers.containsString("Caused by:"));
        Main.main("-sso", "-o", "target/somefolder", "-t", "doesnt-exist", "-debug");
    }

}
