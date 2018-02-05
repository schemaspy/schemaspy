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
import org.schemaspy.model.*;
import org.schemaspy.service.DatabaseService;
import org.schemaspy.service.SqlService;
import org.schemaspy.testing.AssumeClassIsPresentRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.InformixContainer;

import java.io.File;
import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@SpringBootTest
public class InformixIndexIT {
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

    public static TestRule jdbcDriverClassPresentRule = new AssumeClassIsPresentRule("com.informix.jdbc.IfxDriver");

    public static JdbcContainerRule<InformixContainer> jdbcContainerRule =
            new JdbcContainerRule<>(() -> new InformixContainer())
                    .assumeDockerIsPresent()
                    .withAssumptions(assumeDriverIsPresent())
                    .withInitScript("integrationTesting/informixIndexXMLIT/dbScripts/informix.sql");

    @ClassRule
    public static final TestRule chain = RuleChain
            .outerRule(jdbcContainerRule)
            .around(jdbcDriverClassPresentRule);

    @Before
    public synchronized void gatheringSchemaDetailsTest() throws SQLException, IOException {
        if (database == null) {
            createDatabaseRepresentation();
        }
    }

    private void createDatabaseRepresentation() throws SQLException, IOException {
        String[] args = {
                "-t", "informix",
                "-db", "test",
                "-s", "informix",
                "-cat", "test",
                "-server", "dev",
                "-o", "target/integrationtesting/informix",
                "-u", jdbcContainerRule.getContainer().getUsername(),
                "-p", jdbcContainerRule.getContainer().getPassword(),
                "-host", jdbcContainerRule.getContainer().getContainerIpAddress(),
                "-port", jdbcContainerRule.getContainer().getJdbcPort().toString()
        };
        given(arguments.getOutputDirectory()).willReturn(new File("target/integrationtesting/informix"));
        given(arguments.getDatabaseType()).willReturn("informix");
        given(arguments.getUser()).willReturn(jdbcContainerRule.getContainer().getUsername());
        given(arguments.getSchema()).willReturn("informix");
        given(arguments.getCatalog()).willReturn("test");
        given(arguments.getDatabaseName()).willReturn("test");
        Config config = new Config(args);
        DatabaseMetaData databaseMetaData = sqlService.connect(config);
        Database database = new Database(config, databaseMetaData, arguments.getDatabaseName(), arguments.getCatalog(), arguments.getSchema(), null, progressListener);
        databaseService.gatheringSchemaDetails(config, database, progressListener);
        this.database = database;
    }

    @Test
    public void databaseShouldBePopulatedWithTableTest() {
        Table table = getTable("test");
        assertThat(table).isNotNull();
    }

    @Test
    public void databaseShouldBePopulatedWithTableTestAndHaveColumnName() {
        Table table = getTable("test");
        TableColumn column = table.getColumn("firstname");
        assertThat(column).isNotNull();
    }

    @Test
    public void tableTestShouldHaveTwoIndexes() {
        Table table = getTable("test");
        assertThat(table.getIndexes().size()).isEqualTo(2);
    }

    @Test
    public void tableTestIndex_test_index_shouldHaveThreeColumns() {
        TableIndex index = getTable("test").getIndex("test_index");
        assertThat(index.getColumns().size()).isEqualTo(3);
    }

    private Table getTable(String tableName) {
        return database.getTablesByName().get(tableName);
    }
}
