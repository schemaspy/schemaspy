package org.schemaspy.testcontainer;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.schemaspy.Config;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.schemaspy.service.DatabaseService;
import org.schemaspy.service.SqlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.jdbc.ContainerDatabaseDriver;

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
public class MSSQLServerIT {

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
    public static JdbcContainerRule<MSSQLServerContainer> jdbcContainerRule =
            new JdbcContainerRule<>(() -> new MSSQLServerContainer())
                    .assumeDockerIsPresent()
                    .withAssumptions(assumeDriverIsPresent())
                    .withInitScript("integrationTesting/dbScripts/mssql.sql");

    @Before
    public synchronized void gatheringSchemaDetailsTest() throws SQLException, IOException, ScriptException, URISyntaxException {
        if (database == null) {
            createDatabaseRepresentation();
        }
    }

    private void createDatabaseRepresentation() throws SQLException, IOException, URISyntaxException {
        String[] args = {
                "-t", "mssql08",
                "-db", "test",
                "-s", "dbo",
                "-cat", "%",
                "-o", "target/integrationtesting/mssql",
                "-u", "sa",
                "-p", jdbcContainerRule.getContainer().getPassword(),
                "-host", jdbcContainerRule.getContainer().getContainerIpAddress(),
                "-port", jdbcContainerRule.getContainer().getMappedPort(1433).toString()
        };
        given(arguments.getOutputDirectory()).willReturn(new File("target/integrationtesting/mssql"));
        given(arguments.getDatabaseType()).willReturn("mssql08");
        given(arguments.getUser()).willReturn("sa");
        given(arguments.getSchema()).willReturn("dbo");
        given(arguments.getCatalog()).willReturn("%");
        given(arguments.getDatabaseName()).willReturn("test");
        Config config = new Config(args);
        DatabaseMetaData databaseMetaData = sqlService.connect(config);
        Database database = new Database(config, databaseMetaData, arguments.getDatabaseName(), arguments.getCatalog(), arguments.getSchema(), null, progressListener);
        databaseService.gatheringSchemaDetails(config, database, progressListener);
        this.database = database;
    }

    @Test
    public void databaseShouldBePopulatedWithTableTest() {
        Table table = getTable("TestTable");
        assertThat(table).isNotNull();
    }

    @Test
    public void databaseShouldBePopulatedWithTableTestAndHaveColumnName() {
        Table table = getTable("TestTable");
        TableColumn column = table.getColumn("Description");
        assertThat(column).isNotNull();
    }

    @Test
    public void databaseShouldBePopulatedWithTableTestAndHaveColumnNameWithComment() {
        Table table = getTable("TestTable");
        TableColumn column = table.getColumn("Description");
        assertThat(column.getComments()).isEqualToIgnoringCase("This is column description");
    }

    private Table getTable(String tableName) {
        return database.getTablesByName().get(tableName);
    }
}
