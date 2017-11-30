package org.schemaspy.testcontainer;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.schemaspy.Config;
import org.schemaspy.Main;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.model.*;
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
import org.testcontainers.containers.MySQLContainer;

import javax.script.ScriptException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MysqlSpacesNoDotsIT {

    @ClassRule
    public static JdbcContainerRule<MySQLContainer> jdbcContainerRule =
            new JdbcContainerRule<>(() -> new MySQLContainer())
                    .assumeDockerIsPresent()
                    .withAssumptions(assumeDriverIsPresent())
                    .withInitScript("integrationTesting/dbScripts/mysql_spaces_no_dots.sql")
                    .withInitUser("root", "test");

    @Configuration
    @ComponentScan(basePackages = {"org.schemaspy"}, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = Main.class))
    static class MysqlSpacesNoDotsITConfig {
        @Bean
        public ApplicationArguments applicationArguments() {
            return new DefaultApplicationArguments(new String[]{
                    "-t", "mysql",
                    "-db", "TEST 1",
                    "-s", "TEST 1",
                    "-cat", "%",
                    "-o", "target/integrationtesting/mysql_spaces_no_dots",
                    "-u", "test",
                    "-p", "test",
                    "-host", jdbcContainerRule.getContainer().getContainerIpAddress(),
                    "-port", jdbcContainerRule.getContainer().getMappedPort(3306).toString()
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
    public synchronized void createDatabaseRepresentation() throws SQLException, IOException, ScriptException, URISyntaxException {
        if (database == null) {
            doCreateDatabaseRepresentation();
        }
    }

    private void doCreateDatabaseRepresentation() throws SQLException, IOException, URISyntaxException {
        DatabaseMetaData databaseMetaData = sqlService.connect(config);
        Database database = new Database(config, databaseMetaData, arguments.getDatabaseName(), arguments.getCatalog(), arguments.getSchema(), null, progressListener);
        databaseService.gatheringSchemaDetails(config, database, progressListener);
        this.database = database;
    }

    @Test
    public void databaseShouldExist() {
        assertThat(database).isNotNull();
        assertThat(database.getName()).isEqualToIgnoringCase("TEST 1");
    }

    @Test
    public void databaseShouldHaveTable() {
        assertThat(database.getTables()).extracting(Table::getName).contains("TABLE 1");
    }

    @Test
    public void tableShouldHavePKWithAutoIncrement() {
        assertThat(database.getTablesByName().get("TABLE 1").getColumns()).extracting(TableColumn::getName).contains("id");
        assertThat(database.getTablesByName().get("TABLE 1").getColumn("id").isPrimary()).isTrue();
        assertThat(database.getTablesByName().get("TABLE 1").getColumn("id").isAutoUpdated()).isTrue();
    }

    @Test
    public void tableShouldHaveForeignKey() {
        assertThat(database.getTablesByName().get("TABLE 1").getForeignKeys()).extracting(ForeignKeyConstraint::getName).contains("link fk");
    }

    @Test
    public void tableShouldHaveUniqueKey() {
        assertThat(database.getTablesByName().get("TABLE 1").getIndexes()).extracting(TableIndex::getName).contains("name_link_unique");
    }

    @Test
    public void tableShouldHaveColumnWithSpaceInIt() {
        assertThat(database.getTablesByName().get("TABLE 1").getColumns()).extracting(TableColumn::getName).contains("link id");
    }
}
