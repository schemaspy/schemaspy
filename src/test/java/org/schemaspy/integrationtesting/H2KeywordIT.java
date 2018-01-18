package org.schemaspy.integrationtesting;

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
import org.schemaspy.testing.H2MemoryRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class H2KeywordIT {

    @ClassRule
    public static H2MemoryRule h2MemoryRule = new H2MemoryRule("h2keyword").addSqlScript("src/test/resources/integrationTesting/h2KeywordIT/dbScripts/keyword_in_table.sql");

    @Configuration
    @ComponentScan(basePackages = {"org.schemaspy"}, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = Main.class))
    static class H2KeywordITConfig {
        @Bean
        public ApplicationArguments applicationArguments() {
            return new DefaultApplicationArguments(new String[]{
                    "-t", "src/test/resources/integrationTesting/dbTypes/h2memory",
                    "-db", "h2keyword",
                    "-s", "h2keyword",
                    "-cat", "PUBLIC",
                    "-o", "target/integrationtesting/h2keyword",
                    "-u", "sa"
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
    public synchronized void createDatabaseRepresentation() throws SQLException, IOException {
        if (database == null) {
            doCreateDatabaseRepresentation();
        }
    }

    private void doCreateDatabaseRepresentation() throws SQLException, IOException {
        DatabaseMetaData databaseMetaData = sqlService.connect(config);
        Database database = new Database(config, databaseMetaData, arguments.getDatabaseName(), arguments.getCatalog(), arguments.getSchema(), null, progressListener);
        databaseService.gatheringSchemaDetails(config, database, progressListener);
        this.database = database;
    }

    @Test
    public void databaseShouldExist() {
        assertThat(database).isNotNull();
        assertThat(database.getName()).isEqualToIgnoringCase("h2keyword");
    }

    @Test
    public void tableWithKeyWordShouldExist() {
        assertThat(database.getTables()).extracting(t -> t.getName()).contains("DISTINCT");
    }
}
