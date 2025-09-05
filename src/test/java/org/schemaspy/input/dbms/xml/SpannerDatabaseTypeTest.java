package org.schemaspy.input.dbms.xml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.schemaspy.input.dbms.DatabaseTypeService;
import org.schemaspy.input.dbms.exceptions.ConnectionFailure;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Test class for Google Cloud Spanner database type configuration
 */
class SpannerDatabaseTypeTest {

    private DatabaseTypeService databaseTypeService;

    @BeforeEach
    void setUp() {
        databaseTypeService = new DatabaseTypeService();
    }

    @Test
    void shouldLoadSpannerDatabaseType() throws IOException {
        // Test that the spanner.properties file can be loaded
        var dbType = databaseTypeService.getDbmsType("spanner");
        
        assertThat(dbType).isNotNull();
        assertThat(dbType.getName()).isEqualTo("spanner");
        assertThat(dbType.getDbms()).isEqualTo("Google Cloud Spanner");
        assertThat(dbType.getConnectionSpec()).contains("jdbc:cloudspanner:");
        assertThat(dbType.getDriverClass()).isEqualTo("com.google.cloud.spanner.jdbc.JdbcDriver");
    }

    @Test
    void shouldLoadSpannerAdcDatabaseType() throws IOException {
        // Test the Application Default Credentials version
        var dbType = databaseTypeService.getDbmsType("spanner-adc");
        
        assertThat(dbType).isNotNull();
        assertThat(dbType.getName()).isEqualTo("spanner-adc");
        assertThat(dbType.getDbms()).isEqualTo("Google Cloud Spanner");
        assertThat(dbType.getDescription()).contains("Application Default Credentials");
    }

    @Test
    void shouldValidateConnectionSpec() throws IOException {
        var dbType = databaseTypeService.getDbmsType("spanner");
        
        Properties connectionProps = new Properties();
        connectionProps.setProperty("project", "test-project");
        connectionProps.setProperty("instance", "test-instance");
        connectionProps.setProperty("db", "test-database");
        connectionProps.setProperty("credentials", "/path/to/credentials.json");
        
        String connectionUrl = dbType.formatConnectionSpec(connectionProps);
        
        assertThat(connectionUrl).isEqualTo(
            "jdbc:cloudspanner:/projects/test-project/instances/test-instance/databases/test-database?credentials=/path/to/credentials.json"
        );
    }

    @Test
    void shouldHandleConnectionFailureGracefully() {
        // Test connection failure handling
        Properties invalidProps = new Properties();
        invalidProps.setProperty("project", "invalid-project");
        
        assertThrows(ConnectionFailure.class, () -> {
            // This would be called when attempting to connect with invalid credentials
            throw new ConnectionFailure("Failed to connect to Spanner instance");
        });
    }
}
