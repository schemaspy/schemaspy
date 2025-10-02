/*
 * Copyright (C) 2024 Nils Petzaell
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
package org.schemaspy.testing.testcontainers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Properties;

/**
 * Custom Testcontainers {@link JdbcDatabaseContainer} for Google Cloud Spanner Emulator.
 * This class wraps the Spanner emulator container to provide a JDBC-compatible interface
 * for SchemaSpy integration tests.
 */
public class SpannerContainer extends JdbcDatabaseContainer<SpannerContainer> {

    private static final String DRIVER_CLASS_NAME = "com.google.cloud.spanner.jdbc.JdbcDriver";
    private static final int SPANNER_PORT = 9010; // Default port for Spanner emulator gRPC
    private static final int SPANNER_HTTP_PORT = 9020; // HTTP port for REST API

    private final String projectId;
    private final String instanceId;
    private final String databaseId;
    private GenericContainer<?> emulator;

    /**
     * Creates a Spanner container with default IDs.
     *
     * @param dockerImageName Docker image for Spanner emulator
     */
    public SpannerContainer(DockerImageName dockerImageName) {
        this(dockerImageName, "test-project", "test-instance", "test-database");
    }

    /**
     * Creates a Spanner container with specified IDs.
     *
     * @param dockerImageName Docker image for Spanner emulator
     * @param projectId Google Cloud project ID to use for testing
     * @param instanceId Spanner instance ID
     * @param databaseId Spanner database ID
     */
    public SpannerContainer(DockerImageName dockerImageName, String projectId, String instanceId, String databaseId) {
        super(dockerImageName);
        this.projectId = projectId;
        this.instanceId = instanceId;
        this.databaseId = databaseId;
        
        // Initialize the generic container for the emulator
        this.emulator = new GenericContainer<>(dockerImageName)
                .withExposedPorts(SPANNER_PORT, SPANNER_HTTP_PORT)
                .waitingFor(Wait.forLogMessage(".*Cloud Spanner emulator running.*", 1))
                .withStartupTimeout(Duration.ofMinutes(2));
    }

    @Override
    public String getDriverClassName() {
        return DRIVER_CLASS_NAME;
    }

    @Override
    public String getJdbcUrl() {
        if (emulator != null && emulator.isRunning()) {
            String host = emulator.getHost();
            Integer port = emulator.getMappedPort(SPANNER_PORT);
            // JDBC URL format for emulator with autoConfigEmulator=true
            return String.format(
                    "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s?autoConfigEmulator=true;emulatorHost=%s:%d",
                    projectId, instanceId, databaseId, host, port
            );
        }
        return "";
    }

    @Override
    public String getUsername() {
        return ""; // Emulator doesn't require authentication
    }

    @Override
    public String getPassword() {
        return ""; // Emulator doesn't require authentication
    }

    @Override
    protected String getTestQueryString() {
        return "SELECT 1";
    }

    @Override
    public String getDatabaseName() {
        return databaseId;
    }

    @Override
    protected void configure() {
        // Configuration is delegated to the emulator container
        if (emulator != null) {
            addExposedPorts(SPANNER_PORT, SPANNER_HTTP_PORT);
        }
    }

    @Override
    public void start() {
        if (emulator != null) {
            emulator.start();
        }
    }

    @Override
    public void stop() {
        if (emulator != null) {
            emulator.stop();
        }
    }

    @Override
    public String getHost() {
        if (emulator != null && emulator.isRunning()) {
            return emulator.getHost();
        }
        return "localhost";
    }

    @Override
    public Integer getMappedPort(int originalPort) {
        if (emulator != null && emulator.isRunning()) {
            return emulator.getMappedPort(originalPort);
        }
        return originalPort;
    }

    /**
     * Gets the Google Cloud project ID.
     *
     * @return project ID
     */
    public String getProjectId() {
        return projectId;
    }

    /**
     * Gets the Spanner instance ID.
     *
     * @return instance ID
     */
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * Gets the Spanner database ID.
     *
     * @return database ID
     */
    public String getDatabaseId() {
        return databaseId;
    }

    /**
     * Gets the emulator gRPC endpoint for direct Spanner client connections.
     *
     * @return emulator endpoint (host:port)
     */
    public String getEmulatorEndpoint() {
        if (emulator != null && emulator.isRunning()) {
            return String.format("%s:%d", getHost(), getMappedPort(SPANNER_PORT));
        }
        return "";
    }

    /**
     * Gets the emulator HTTP endpoint for REST API access.
     *
     * @return emulator HTTP endpoint (host:port)
     */
    public String getEmulatorHttpEndpoint() {
        if (emulator != null && emulator.isRunning()) {
            return String.format("%s:%d", getHost(), getMappedPort(SPANNER_HTTP_PORT));
        }
        return "";
    }

    @Override
    public Connection createConnection(String queryString) throws SQLException {
        Properties info = new Properties();
        info.put("user", getUsername());
        info.put("password", getPassword());
        // For Spanner emulator, authentication is handled via the JDBC URL
        String url = getJdbcUrl();
        if (queryString != null && !queryString.isEmpty()) {
            url += queryString;
        }
        return DriverManager.getConnection(url, info);
    }

    /**
     * Sets the startup timeout for the container.
     *
     * @param startupTimeout timeout duration
     * @return this container instance
     */
    public SpannerContainer withStartupTimeout(Duration startupTimeout) {
        if (emulator != null) {
            emulator.withStartupTimeout(startupTimeout);
        }
        return this;
    }
}
