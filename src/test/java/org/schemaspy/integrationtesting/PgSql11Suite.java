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

import org.junit.platform.suite.api.*;
import org.schemaspy.testing.SQLScriptsRunner;
import org.schemaspy.testing.SuiteContainerExtension;
import org.testcontainers.containers.PostgreSQLContainer;

@Suite
@SuiteDisplayName("pgsql 11 Test Suite")
@SelectPackages("org.schemaspy.integrationtesting.pgsql.v11")
@IncludeClassNamePatterns(".*IT$")
@IncludeEngines("junit-jupiter")
public class PgSql11Suite {

    public static final SuiteContainerExtension SUITE_CONTAINER =
            new SuiteContainerExtension(
                    () -> new PostgreSQLContainer<>("postgres:15.3")
            )
                    .withInitFunctions(new SQLScriptsRunner("integrationTesting/pgsql/dbScripts/", "\n\n\n"));

}
