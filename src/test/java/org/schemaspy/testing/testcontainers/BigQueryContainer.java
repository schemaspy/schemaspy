/*
 * Copyright (C) 2025 SchemaSpy Contributors
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

import org.testcontainers.containers.BigQueryEmulatorContainer;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Wrapper around BigQueryEmulatorContainer to make it compatible with JdbcDatabaseContainer
 * interface used by SchemaSpy test infrastructure.
 * 
 * This adapter allows the BigQuery emulator to be used with the existing SuiteContainerExtension.
 */
public class BigQueryContainer extends JdbcDatabaseContainer<BigQueryContainer> {

    private static final DockerImageName DEFAULT_IMAGE_NAME = 
            DockerImageName.parse("ghcr.io/goccy/bigquery-emulator:latest");
    
    private static final String DRIVER_CLASS_NAME = "com.simba.googlebigquery.jdbc.Driver";
    
    private static final int BIGQUERY_PORT = 9050;
    
    private final BigQueryEmulatorContainer emulator;
    private final String projectId;
    
    /**
     * Creates a BigQuery container with default project ID "test-project".
     */
    public BigQueryContainer() {
        this(DEFAULT_IMAGE_NAME);
    }
    
    /**
     * Creates a BigQuery container with specified Docker image.
     * 
     * @param dockerImageName Docker image for BigQuery emulator
     */
    public BigQueryContainer(DockerImageName dockerImageName) {
        this(dockerImageName, "test-project");
    }
    
    /**
     * Creates a BigQuery container with specified project ID.
     * 
     * @param dockerImageName Docker image for BigQuery emulator
     * @param projectId Google Cloud project ID to use for testing
     */
    public BigQueryContainer(DockerImageName dockerImageName, String projectId) {
        super(dockerImageName);
        this.projectId = projectId;
        this.emulator = new BigQueryEmulatorContainer(dockerImageName);
    }

    @Override
    public String getDriverClassName() {
        return DRIVER_CLASS_NAME;
    }

    @Override
    public String getJdbcUrl() {
        if (emulator != null && emulator.isRunning()) {
            String host = emulator.getHost();
            Integer port = emulator.getMappedPort(BIGQUERY_PORT);
            return String.format(
                    "jdbc:bigquery://http://%s:%d;ProjectId=%s;DefaultDataset=test_dataset",
                    host, port, projectId
            );
        }
        return "";
    }

    @Override
    public String getUsername() {
        return "test";
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    protected String getTestQueryString() {
        return "SELECT 1";
    }

    @Override
    public String getDatabaseName() {
        return "test_dataset";
    }

    @Override
    protected void configure() {
        // Delegate configuration to the emulator
        if (emulator != null) {
            addExposedPort(BIGQUERY_PORT);
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
     * Gets the BigQuery project ID.
     * 
     * @return project ID
     */
    public String getProjectId() {
        return projectId;
    }

    /**
     * Gets the emulator endpoint URL.
     * 
     * @return emulator HTTP endpoint
     */
    public String getEmulatorEndpoint() {
        return String.format("http://%s:%d", 
                emulator.getHost(), 
                emulator.getMappedPort(BIGQUERY_PORT));
    }

    @Override
    public Connection createConnection(String queryString) throws SQLException {
        Properties info = new Properties();
        info.put("user", getUsername());
        info.put("password", getPassword());
        
        String url = getJdbcUrl() + (queryString != null ? queryString : "");
        
        try {
            Driver driver = (Driver) Class.forName(getDriverClassName()).getDeclaredConstructor().newInstance();
            return driver.connect(url, info);
        } catch (Exception e) {
            // Fallback to DriverManager if direct instantiation fails
            return DriverManager.getConnection(url, info);
        }
    }
}
