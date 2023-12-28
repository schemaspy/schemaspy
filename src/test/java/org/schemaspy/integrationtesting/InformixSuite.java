package org.schemaspy.integrationtesting;

import org.junit.platform.suite.api.*;
import org.schemaspy.testing.SQLScriptsRunner;
import org.schemaspy.testing.SuiteContainerExtension;
import org.testcontainers.containers.InformixContainer;

@Suite
@SuiteDisplayName("Informix Test Suite")
@SelectPackages("org.schemaspy.integrationtesting.informix")
@IncludeClassNamePatterns(".*IT$")
@IncludeEngines("junit-jupiter")
public class InformixSuite {

    public static final SuiteContainerExtension SUITE_CONTAINER =
            new SuiteContainerExtension(
                    () -> new InformixContainer()
            )
                    .withInitFunctions(new SQLScriptsRunner("integrationTesting/informix/dbScripts/", "\n\n\n"));

}
