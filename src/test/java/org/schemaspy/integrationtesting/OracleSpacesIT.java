package org.schemaspy.integrationtesting;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.schemaspy.Config;
import org.schemaspy.Main;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.service.DatabaseService;
import org.schemaspy.service.SqlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.OracleContainer;

import javax.script.ScriptException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class OracleSpacesIT {

    @ClassRule
    public static JdbcContainerRule<OracleContainer> jdbcContainerRule =
            new JdbcContainerRule<>(() -> new OracleContainer())
                    .assumeDockerIsPresent()
                    .withAssumptions(assumeDriverIsPresent())
                    .withInitScript("integrationTesting/oracleSpacesIT/dbScripts/spaces_in_table_names.sql");

    @Configuration
    @ComponentScan(basePackages = {"org.schemaspy"}, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = Main.class))
    static class OracleSpacesITConfig {
        @Bean
        public ApplicationArguments applicationArguments() {
            return new DefaultApplicationArguments(new String[]{
                    "-t", "orathin",
                    "-db", jdbcContainerRule.getContainer().getSid(),
                    "-s", "ORASPACEIT",
                    "-cat", "%",
                    "-o", "target/integrationtesting/ORASPACEIT",
                    "-u", "oraspaceit",
                    "-p", "oraspaceit123",
                    "-host", jdbcContainerRule.getContainer().getContainerIpAddress(),
                    "-port", jdbcContainerRule.getContainer().getOraclePort().toString()
            });
        }
    }

    @Autowired
    private SqlService sqlService;

    @Autowired
    private DatabaseService databaseService;

    @Mock
    private ProgressListener progressListener;

    @Autowired
    private Config config;

    @Autowired
    private CommandLineArguments arguments;

    private static Database database;

    @Before
    public synchronized void gatheringSchemaDetailsTest() throws SQLException, IOException, ScriptException, URISyntaxException {
        if (database == null) {
            createDatabaseRepresentation();
        }
    }

    private void createDatabaseRepresentation() throws SQLException, IOException, URISyntaxException {
        DatabaseMetaData databaseMetaData = sqlService.connect(config);
        Database database = new Database(config, databaseMetaData, arguments.getDatabaseName(), arguments.getCatalog(), arguments.getSchema(), null, progressListener);
        databaseService.gatheringSchemaDetails(config, database, progressListener);
        this.database = database;
    }

    @Test
    public void databaseShouldHaveTableWithSpaces() {
        assertThat(database.getTables()).extracting(t -> t.getName()).contains("test 1.0");
    }
}
