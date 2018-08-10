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
import org.schemaspy.integrationtesting.pgsql.PgSqlCheckConstraintsIT;
import org.schemaspy.integrationtesting.pgsql.PgSqlRoutinesIT;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.jdbc.ext.ScriptUtils;
import org.testcontainers.shaded.com.google.common.base.Charsets;
import org.testcontainers.shaded.com.google.common.io.Resources;

import javax.script.ScriptException;
import java.io.IOException;
import java.net.URL;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        PgSqlCheckConstraintsIT.class,
        PgSqlRoutinesIT.class
})
public class PgSqlSuite {

    @ClassRule
    public static JdbcContainerRule<PostgreSQLContainer> jdbcContainerRule =
            new JdbcContainerRule<>(() -> new PostgreSQLContainer("postgres:10"))
                    .assumeDockerIsPresent()
                    .withAssumptions(assumeDriverIsPresent())
                    .withInitFunctions(connection -> {
                        URL resource = Resources.getResource("integrationTesting/pgsql/dbScripts/dvdrental.sql");
                        try {
                            String sql = Resources.toString(resource, Charsets.UTF_8);
                            ScriptUtils.executeSqlScript(connection, resource.getPath(), sql, false, true, ScriptUtils.DEFAULT_COMMENT_PREFIX, "\n\n\n", ScriptUtils.DEFAULT_BLOCK_COMMENT_START_DELIMITER, ScriptUtils.DEFAULT_BLOCK_COMMENT_END_DELIMITER);
                        } catch (ScriptException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
}
