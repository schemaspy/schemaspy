package org.schemaspy.testing.testcontainers;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.JdbcDatabaseContainer;

class ContainerStopper implements ExtensionContext.Store.CloseableResource {

    public final JdbcDatabaseContainer<?> jdbcDatabaseContainer;

    public ContainerStopper(JdbcDatabaseContainer<?> jdbcDatabaseContainer) {
        this.jdbcDatabaseContainer = jdbcDatabaseContainer;
    }

    @Override
    public void close() throws Throwable {
        jdbcDatabaseContainer.stop();
    }
}
