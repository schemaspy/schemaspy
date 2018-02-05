package org.schemaspy.testcontainer;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
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
import org.schemaspy.testing.AssumeClassIsPresentRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.OracleContainer;

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
public class OracleIT {

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

    public static TestRule jdbcDriverClassPresentRule = new AssumeClassIsPresentRule("oracle.jdbc.OracleDriver");

    public static JdbcContainerRule<OracleContainer> jdbcContainerRule =
            new JdbcContainerRule<>(() -> new OracleContainer())
            .assumeDockerIsPresent()
            .withAssumptions(assumeDriverIsPresent())
            .withInitScript("integrationTesting/dbScripts/oracle.sql");

    @ClassRule
    public static final TestRule chain = RuleChain
            .outerRule(jdbcContainerRule)
            .around(jdbcDriverClassPresentRule);

    @Before
    public synchronized void gatheringSchemaDetailsTest() throws SQLException, IOException, ScriptException, URISyntaxException {
        if (database == null) {
            createDatabaseRepresentation();
        }
    }

    private void createDatabaseRepresentation() throws SQLException, IOException, URISyntaxException {
        String[] args = {
                "-t", "orathin",
                "-db", jdbcContainerRule.getContainer().getSid(),
                "-s", "ORAIT",
                "-cat", "%",
                "-o", "target/integrationtesting/orait",
                "-u", "orait",
                "-p", "orait123",
                "-host", jdbcContainerRule.getContainer().getContainerIpAddress(),
                "-port", jdbcContainerRule.getContainer().getOraclePort().toString()
        };
        given(arguments.getOutputDirectory()).willReturn(new File("target/integrationtesting/databaseServiceIT"));
        given(arguments.getDatabaseType()).willReturn("orathin");
        given(arguments.getUser()).willReturn("orait");
        given(arguments.getSchema()).willReturn("ORAIT");
        given(arguments.getCatalog()).willReturn("%");
        given(arguments.getDatabaseName()).willReturn(jdbcContainerRule.getContainer().getSid());
        Config config = new Config(args);
        DatabaseMetaData databaseMetaData = sqlService.connect(config);
        Database database = new Database(config, databaseMetaData, arguments.getDatabaseName(), arguments.getCatalog(), arguments.getSchema(), null, progressListener);
        databaseService.gatheringSchemaDetails(config, database, progressListener);
        this.database = database;
    }

    @Test
    public void databaseShouldBePopulatedWithTableTest() {
        Table table = getTable("TEST");
        assertThat(table).isNotNull();
    }

    @Test
    public void databaseShouldBePopulatedWithTableTestAndHaveColumnName() {
        Table table = getTable("TEST");
        TableColumn column = table.getColumn("NAME");
        assertThat(column).isNotNull();
    }

    @Test
    public void databaseShouldBePopulatedWithTableTestAndHaveColumnNameWithComment() {
        Table table = getTable("TEST");
        TableColumn column = table.getColumn("NAME");
        assertThat(column.getComments()).isEqualToIgnoringCase("the name");
    }

    private Table getTable(String tableName) {
        return database.getTablesByName().get(tableName);
    }
}
