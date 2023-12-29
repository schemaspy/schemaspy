package org.schemaspy.integrationtesting;

import org.junit.platform.suite.api.*;
import org.schemaspy.testing.SQLScriptsRunner;
import org.schemaspy.testing.testcontainers.MSSQLContainer;
import org.schemaspy.testing.testcontainers.SuiteContainerExtension;

@Suite
@SuiteDisplayName("MSSQL Test Suite")
@SelectPackages("org.schemaspy.integrationtesting.mssqlserver")
@IncludeClassNamePatterns(".*IT$")
@IncludeEngines("junit-jupiter")
public class MssqlSuite {

    public static final SuiteContainerExtension SUITE_CONTAINER =
            new SuiteContainerExtension(
                    () -> new MSSQLContainer<>("mcr.microsoft.com/mssql/server:2019-CU3-ubuntu-16.04")
            )
                    .withInitFunctions(new SQLScriptsRunner("integrationTesting/mssqlserver/dbScripts/"))
                    .withInitUser("sa", "A_Str0ng_Required_Password");

}
