package org.schemaspy.testing.testcontainers;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.ext.ScriptUtils;
import org.testcontainers.jdbc.ContainerLessJdbcDelegate;
import org.testcontainers.shaded.com.google.common.io.Resources;

import javax.script.ScriptException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SuiteContainerExtension implements BeforeAllCallback, ExecutionCondition{

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final Namespace NAMESPACE = Namespace.create("schemaspy", "testcontainers");

    private final Supplier<JdbcDatabaseContainer<?>> containerCreator;
    private JdbcDatabaseContainer<?> container;
    private String queryString ="";
    private String initScriptPath;
    private String initUser = null;
    private String initPassword = null;
    private final List<Consumer<Connection>> initFunctions = new ArrayList<>();

    public SuiteContainerExtension(Supplier<JdbcDatabaseContainer<?>> containerCreator) {
        this.containerCreator = containerCreator;
    }

    public SuiteContainerExtension withQueryString(String queryString) {
        this.queryString = queryString;
        return this;
    }

    public SuiteContainerExtension withInitScript(String initScriptPath) {
        this.initScriptPath = initScriptPath;
        return this;
    }

    public SuiteContainerExtension withInitFunctions(Consumer<Connection>...initFunctions) {
        Collections.addAll(this.initFunctions, initFunctions);
        return this;
    }

    public SuiteContainerExtension withInitUser(String user, String password) {
        initUser = user;
        initPassword = password;
        return this;
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        if (context.getParent().isPresent()) {
            Store store = context.getParent().get().getStore(NAMESPACE);
            container = store.getOrComputeIfAbsent(
                    containerCreator,
                    this::startContainer,
                    JdbcDatabaseContainer.class
            );
            store.getOrComputeIfAbsent(container, ContainerStopper::new);
        } else {
            LOGGER.error("This should not happen!!");
        }

    }

    private JdbcDatabaseContainer<?> startContainer(Supplier<JdbcDatabaseContainer<?>> containerSupplier) {
        JdbcDatabaseContainer<?> jdbcDatabaseContainer = containerSupplier.get();
        jdbcDatabaseContainer.start();
        if (initScriptPath != null) {
            try {
                URL resource = Resources.getResource(initScriptPath);
                String sql = Resources.toString(resource, StandardCharsets.UTF_8);
                ScriptUtils.executeDatabaseScript(
                        new ContainerLessJdbcDelegate(getConnection(container)),
                        initScriptPath,
                        sql
                );
            } catch (IOException | IllegalArgumentException e) {
                LOGGER.error("Could not load classpath init script: {}", initScriptPath);
                throw new RuntimeException("Could not load classpath init script: " + initScriptPath, e);
            } catch (ScriptException | SQLException e) {
                LOGGER.error("Error while execution init script: {}", initScriptPath, e);
                throw new RuntimeException("SQLException: ", e);
            }
        }

        for(Consumer<Connection> initFunction : initFunctions) {
            Connection connection = null;
            try {
                connection = getConnection(jdbcDatabaseContainer);
                initFunction.accept(connection);
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOGGER.warn("Failed to execute function: {}", initFunction.getClass().getSimpleName() , e);
            }
        }
        return jdbcDatabaseContainer;
    }

    private Connection getConnection(JdbcDatabaseContainer<?> jdbcDatabaseContainer) throws SQLException {
        if (initUser != null && initPassword != null) {
            Properties info = new Properties();
            info.put("user", initUser);
            info.put("password", initPassword);
            String url = jdbcDatabaseContainer.getJdbcUrl() + queryString;
            Driver jdbcDriverInstance = jdbcDatabaseContainer.getJdbcDriverInstance();
            return jdbcDriverInstance.connect(url, info);
        } else {
            return jdbcDatabaseContainer.createConnection(queryString);
        }
    }

    public String getHost() {
        return container.getHost();
    }

    public String getPort(int port) {
        return String.valueOf(container.getMappedPort(port));
    }

    public String getUsername() {
        return container.getUsername();
    }

    public String getPassword() {
        return container.getPassword();
    }

    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
        return extensionContext.getRoot().getStore(NAMESPACE).getOrComputeIfAbsent("dockerIsPresent", (v) -> isDockerAvailable() , ConditionEvaluationResult.class);
    }

    private ConditionEvaluationResult isDockerAvailable() {
        try {
            DockerClientFactory.instance().client();
            return ConditionEvaluationResult.enabled("Docker is present");
        } catch (Throwable var2) {
            return ConditionEvaluationResult.disabled("Docker isn't present");
        }
    }

}
