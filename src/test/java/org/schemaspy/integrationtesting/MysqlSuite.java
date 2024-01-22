package org.schemaspy.integrationtesting;

import org.junit.platform.suite.api.*;
import org.schemaspy.testing.SQLScriptsRunner;
import org.schemaspy.testing.testcontainers.SuiteContainerExtension;
import org.testcontainers.containers.MySQLContainer;

@Suite
@SuiteDisplayName("Mysql Test Suite")
@SelectPackages("org.schemaspy.integrationtesting.mysql")
@IncludeClassNamePatterns(".*IT$")
@IncludeEngines("junit-jupiter")
public class MysqlSuite {


    public static final SuiteContainerExtension SUITE_CONTAINER
            = new SuiteContainerExtension(
                    () -> new MySQLContainer<>("mysql:8.2-oracle")
            .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci")
    )
            .withInitFunctions(new SQLScriptsRunner("integrationTesting/mysql/dbScripts/"))
            .withInitUser("root", "test")
            .withQueryString("?useSSL=false&allowPublicKeyRetrieval=true");

}
