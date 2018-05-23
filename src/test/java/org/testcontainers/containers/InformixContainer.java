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
package org.testcontainers.containers;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import org.testcontainers.containers.wait.HostPortWaitStrategy;
import org.testcontainers.containers.wait.LogMessageWaitStrategy;
import org.testcontainers.containers.wait.WaitStrategy;

import java.lang.reflect.Field;

/**
 * @author Nils Petzaell
 */
public class InformixContainer<SELF extends InformixContainer<SELF>> extends JdbcDatabaseContainer<SELF> {

    static final String IMAGE = "ibmcom/informix-developer-database";
    static final Integer INFORMIX_PORT = 9088;

    private final WaitStrategy logMessageWaitStrategy = new LogMessageWaitStrategy().withRegEx(".*Startup of dev SUCCESS.*\r\n");
    private final WaitStrategy hostPortWaitStrategy = new HostPortWaitStrategy();

    public InformixContainer(){
        this(IMAGE + ":latest");
    }

    public InformixContainer(final String dockerImageName) {
        super(dockerImageName);
    }



    @Override
    protected String getDriverClassName() {
        return "com.informix.jdbc.IfxDriver";
    }

    @Override
    public String getJdbcUrl() {
        return "jdbc:informix-sqli://"+ getContainerIpAddress() + ":" + getMappedPort(INFORMIX_PORT)+ "/sysmaster:INFORMIXSERVER=dev";
    }

    @Override
    public String getUsername() {
        return "informix";
    }

    @Override
    public String getPassword() {
        return "in4mix";
    }

    public Integer getJdbcPort(){
        return getMappedPort(INFORMIX_PORT);
    }

    @Override
    protected String getTestQueryString() {
        return "select count(*) from systables";
    }

    @Override
    protected Integer getLivenessCheckPort() {
        return getMappedPort(INFORMIX_PORT);
    }

    @Override
    protected void configure() {
        super.configure();
        addExposedPort(INFORMIX_PORT);
        addEnv("LICENSE","accept");
        withPrivilegedMode(true);
        withCreateContainerCmdModifier(c -> {
            c.withTty(true);
            c.withPublishAllPorts(false);
            c.withPortBindings(new Ports(ExposedPort.tcp(9088), Ports.Binding.empty()));
        });
    }

    @Override
    protected void waitUntilContainerStarted() {
        logMessageWaitStrategy.waitUntilReady(this);
        dockerClient.restartContainerCmd(containerId).exec();
        updateContainerInfo();
        super.waitUntilContainerStarted();
    }

    private void updateContainerInfo() {
        Field field = null;
        try {
            field = GenericContainer.class.getDeclaredField("containerInfo");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        field.setAccessible(true);
        try {
            field.set(this, dockerClient.inspectContainerCmd(containerId).exec());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        field.setAccessible(false);
    }
}
