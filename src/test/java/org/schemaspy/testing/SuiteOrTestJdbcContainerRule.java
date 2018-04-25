/*
 * Copyright (c) 2018 Nils Petzaell
 *
 * This file is part of SchemaSpy.
 *
 *  SchemaSpy is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  SchemaSpy is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.schemaspy.testing;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.JdbcDatabaseContainer;

import java.util.Objects;

public class SuiteOrTestJdbcContainerRule<T extends JdbcDatabaseContainer> extends JdbcContainerRule {

    private JdbcContainerRule<T> jdbcContainerRule;
    private JdbcContainerRule<T> suiteJdbcContainerRule;
    private JdbcContainerRule<T> active;

    public SuiteOrTestJdbcContainerRule(JdbcContainerRule<T> suiteJdbcContainerRule, JdbcContainerRule<T> jdbcContainerRule) {
        super(null);
        this.suiteJdbcContainerRule = suiteJdbcContainerRule;
        this.jdbcContainerRule = jdbcContainerRule;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        if (Objects.isNull(suiteJdbcContainerRule.getContainer())) {
            active = jdbcContainerRule;
            return jdbcContainerRule.apply(base, description);
        } else {
            active = suiteJdbcContainerRule;
            return base;
        }
    }

    @Override
    public GenericContainer getContainer() {
        return active.getContainer();
    }
}
