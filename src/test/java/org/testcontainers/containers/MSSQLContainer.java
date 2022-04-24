/*
 * Copyright (C) 2020 Nils Petzaell
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
package org.testcontainers.containers;

import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;

import java.util.Collections;
import java.util.Set;

public class MSSQLContainer extends MSSQLServerContainer {

    public MSSQLContainer(String dockerImageName) {
        super(dockerImageName);
    }

    @Override
    protected void configure() {
        super.configure();
        addEnv("MSSQL_COLLATION", "SQL_Latin1_General_CP1_CS_AS");
    }

    @Override
    public Set<Integer> getLivenessCheckPortNumbers() {
        return Collections.singleton(getMappedPort(MS_SQL_SERVER_PORT));
    }

    @Override
    protected void waitUntilContainerStarted() {
        new WaitAllStrategy()
                .withStrategy(Wait.forListeningPort())
                .withStrategy(Wait.forLogMessage("(?s).*default collation was successfully changed.*",1))
                .waitUntilReady(this);
        super.waitUntilContainerStarted();
    }

    @Override
    public String getUsername() {
        return "sa";
    }
}
