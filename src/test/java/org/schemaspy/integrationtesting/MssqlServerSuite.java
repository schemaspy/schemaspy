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

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.schemaspy.integrationtesting.mssqlserver.MSSQLServerCheckConstraintIT;
import org.schemaspy.integrationtesting.mssqlserver.MSSQLServerCommentsIT;
import org.schemaspy.integrationtesting.mssqlserver.MSSQLServerHTMLIT;
import org.schemaspy.integrationtesting.mssqlserver.MSSQLServerRemoteTablesIT;
import org.schemaspy.testing.SQLScriptsRunner;
import org.testcontainers.containers.MSSQLServerContainer;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        MSSQLServerCheckConstraintIT.class,
        MSSQLServerHTMLIT.class,
        MSSQLServerCommentsIT.class,
        MSSQLServerRemoteTablesIT.class
})
public class MssqlServerSuite {

    @ClassRule
    public static JdbcContainerRule<MSSQLServerContainer> jdbcContainerRule =
            new JdbcContainerRule<>(() -> new MSSQLServerContainer("microsoft/mssql-server-linux:2017-CU6"))
                    .assumeDockerIsPresent()
                    .withAssumptions(assumeDriverIsPresent())
                    .withInitFunctions(new SQLScriptsRunner("integrationTesting/mssqlserver/dbScripts/"));
}
