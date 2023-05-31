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
import org.schemaspy.integrationtesting.pgsql.*;
import org.schemaspy.testing.SQLScriptsRunner;
import org.testcontainers.containers.PostgreSQLContainer;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        PgSqlCheckConstraintsIT.class,
        PgSqlMaterializedViewsIT.class,
        PgSqlRelationshipErrorIT.class,
        PgSqlRoutinesIT.class,
        PgSqlTypesIT.class
})
public class PgSqlSuite {

    @ClassRule
    @SuppressWarnings("unchecked")
    public static JdbcContainerRule<PostgreSQLContainer<?>> jdbcContainerRule =
            new JdbcContainerRule<PostgreSQLContainer<?>>(() -> new PostgreSQLContainer<>("postgres:10.4"))
                    .assumeDockerIsPresent()
                    .withAssumptions(assumeDriverIsPresent())
                    .withInitFunctions(new SQLScriptsRunner("integrationTesting/pgsql/dbScripts/", "\n\n\n"));
}
