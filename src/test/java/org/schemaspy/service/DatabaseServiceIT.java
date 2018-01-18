package org.schemaspy.service;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.schemaspy.Config;
import org.schemaspy.Main;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
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

import java.sql.DatabaseMetaData;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DatabaseServiceIT {

    private static String CREATE_SCHEMA = "CREATE SCHEMA DATABASESERVICEIT AUTHORIZATION SA";
    private static String SET_SCHEMA = "SET SCHEMA DATABASESERVICEIT";
    private static String CREATE_TABLE = "CREATE TABLE TEST(ID INT PRIMARY KEY, NAME VARCHAR(255))";

    @Rule
    public H2MemoryRule h2MemoryRule = new H2MemoryRule("DatabaseServiceIT").addSqls(CREATE_SCHEMA, SET_SCHEMA, CREATE_TABLE);

    @Configuration
    @ComponentScan(basePackages = {"org.schemaspy"}, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = Main.class))
    static class DatabaseServiceITConfig {
        @Bean
        public ApplicationArguments applicationArguments() {
            return new DefaultApplicationArguments(new String[]{
                    "-t", "src/test/resources/integrationTesting/dbTypes/h2memory",
                    "-db", "DatabaseServiceIT",
                    "-s", "DATABASESERVICEIT",
                    "-o", "target/integrationtesting/databaseServiceIT",
                    "-u", "sa"
            });
        }
    }

    @Autowired
    private Config config;

    @Autowired
    private SqlService sqlService;

    @Autowired
    private DatabaseService databaseService;

    @Mock
    private ProgressListener progressListener;

    @Test
    public void gatheringSchemaDetailsTest() throws Exception {
        DatabaseMetaData databaseMetaData = sqlService.connect(config);
        String schema = h2MemoryRule.getConnection().getSchema();
        String catalog = h2MemoryRule.getConnection().getCatalog();
        Database database = new Database(null, databaseMetaData, "DatabaseServiceIT", catalog, schema, null, progressListener);
        databaseService.gatheringSchemaDetails(config, database, progressListener);

        assertThat(database.getTables()).hasSize(1);
    }
}
