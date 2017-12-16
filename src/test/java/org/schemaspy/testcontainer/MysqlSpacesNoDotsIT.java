package org.schemaspy.testcontainer;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.schemaspy.Config;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.model.*;
import org.schemaspy.service.DatabaseService;
import org.schemaspy.service.SqlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.MySQLContainer;

import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MysqlSpacesNoDotsIT {

    @Autowired
    private SqlService sqlService;

    @Autowired
    private DatabaseService databaseService;

    @Mock
    private ProgressListener progressListener;

    @MockBean
    private CommandLineArguments arguments;

    @MockBean
    private CommandLineRunner commandLineRunner;

    private static Database database;

    @ClassRule
    public static JdbcContainerRule<MySQLContainer> jdbcContainerRule =
            new JdbcContainerRule<>(() -> new MySQLContainer())
                    .assumeDockerIsPresent()
                    .withAssumptions(assumeDriverIsPresent())
                    .withInitScript("integrationTesting/dbScripts/mysql_spaces_no_dots.sql")
                    .withInitUser("root", "test");

    @Before
    public synchronized void createDatabaseRepresentation() throws SQLException, IOException, ScriptException, URISyntaxException {
        if (database == null) {
            doCreateDatabaseRepresentation();
        }
    }

    private void doCreateDatabaseRepresentation() throws SQLException, IOException, URISyntaxException {
        String[] args = {
                "-t", "mysql",
                "-db", "TEST 1",
                "-s", "TEST 1",
                "-cat", "%",
                "-o", "target/integrationtesting/mysql_spaces_no_dots",
                "-u", "test",
                "-p", "test",
                "-host", jdbcContainerRule.getContainer().getContainerIpAddress(),
                "-port", jdbcContainerRule.getContainer().getMappedPort(3306).toString()
        };
        given(arguments.getOutputDirectory()).willReturn(new File("target/integrationtesting/mysql_spaces_no_dots"));
        given(arguments.getDatabaseType()).willReturn("mysql");
        given(arguments.getUser()).willReturn("test");
        given(arguments.getSchema()).willReturn("TEST 1");
        given(arguments.getCatalog()).willReturn("%");
        given(arguments.getDatabaseName()).willReturn("TEST 1");
        Config config = new Config(args);
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
