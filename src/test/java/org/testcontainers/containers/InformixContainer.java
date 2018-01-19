package org.testcontainers.containers;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import org.testcontainers.containers.wait.HostPortWaitStrategy;
import org.testcontainers.containers.wait.LogMessageWaitStrategy;
import org.testcontainers.containers.wait.WaitStrategy;

import java.lang.reflect.Field;

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
