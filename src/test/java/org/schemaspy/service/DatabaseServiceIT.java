package org.schemaspy.service;

import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.schemaspy.Config;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.testing.H2MemoryRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DatabaseServiceIT {

  @Configuration
  public static class DatabaseServiceITConfiguration {

    @Bean
    public DatabaseService databaseService() {
      return new DatabaseService();
    }

    @Bean
    public TableService tableService() {
      return new TableService();
    }

    @Bean
    public ViewService viewService() {
      return new ViewService();
    }

    @Bean
    public SqlService sqlService() {
      return new SqlService();
    }
  }

  private static String CREATE_SCHEMA = "CREATE SCHEMA DATABASESERVICEIT AUTHORIZATION SA";
  private static String SET_SCHEMA = "SET SCHEMA DATABASESERVICEIT";
  private static String CREATE_TABLE = "CREATE TABLE TEST(ID INT PRIMARY KEY, NAME VARCHAR(255))";

  @Rule
  public H2MemoryRule h2MemoryRule = new H2MemoryRule("DatabaseServiceIT", CREATE_SCHEMA, SET_SCHEMA, CREATE_TABLE);

  @Autowired
  private SqlService sqlService;

  @Autowired
  private DatabaseService databaseService;

  @Mock
  private ProgressListener prograssListener;

  @Test
  public void gatheringSchemaDetailsTest() throws SQLException, IOException {
    Config config = new Config("-t", "src/test/resources/integrationTesting/h2memory", "-db", "DatabaseServiceIT", "-s", "DATABASESERVICEIT", "-o", "target/integrationtesting/databaseServiceIT", "-u", "sa");
    DatabaseMetaData databaseMetaData = sqlService.connect(config);
    String schema = h2MemoryRule.getConnection().getSchema();
    String catalog = h2MemoryRule.getConnection().getCatalog();
    Database database = new Database(null, databaseMetaData, "DatabaseServiceIT", catalog, schema, null, prograssListener);
    databaseService.gatheringSchemaDetails(config, database, prograssListener);
    assertThat(database.getTables().size()).isEqualTo(1);
  }
}
