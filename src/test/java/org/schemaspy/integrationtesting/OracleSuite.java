package org.schemaspy.integrationtesting;

import org.junit.platform.suite.api.*;
import org.schemaspy.testing.SQLScriptsRunner;
import org.schemaspy.testing.SuiteContainerExtension;
import org.testcontainers.containers.OracleContainer;

@Suite
@SuiteDisplayName("Oracle Test Suite")
@SelectPackages("org.schemaspy.integrationtesting.oracle")
@IncludeClassNamePatterns(".*IT$")
@IncludeEngines("junit-jupiter")
public class OracleSuite {

    public static final SuiteContainerExtension SUITE_CONTAINER =
            new SuiteContainerExtension(
                    () -> new OracleContainer("gvenzl/oracle-xe:11-slim")
                            .usingSid()
            )
                    .withInitFunctions(new SQLScriptsRunner("integrationTesting/oracle/dbScripts/"));

}
