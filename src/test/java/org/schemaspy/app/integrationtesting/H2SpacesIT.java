package org.schemaspy.app.integrationtesting;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.schemaspy.Config;
import org.schemaspy.app.cli.CommandLineArguments;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.service.DatabaseService;
import org.schemaspy.service.SqlService;
import org.schemaspy.testing.H2MemoryRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@SpringBootTest
public class H2SpacesIT {

    @ClassRule
    public static H2MemoryRule h2MemoryRule = new H2MemoryRule("h2 spaces").addSqlScript("src/test/resources/integrationTesting/h2SpacesIT/dbScripts/spaces_in_schema_and_table.sql");

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

    @Before
    public synchronized void createDatabaseRepresentation() throws SQLException, IOException, ScriptException, URISyntaxException {
        if (database == null) {
            doCreateDatabaseRepresentation();
        }
    }

    private void doCreateDatabaseRepresentation() throws SQLException, IOException, URISyntaxException {
        String[] args = {
                "-t", "src/test/resources/integrationTesting/dbTypes/h2memory",
                "-db", "h2 spaces",
                "-s", "h2 spaces",
                "-o", "target/integrationtesting/h2 spaces",
                "-u", "sa"
        };
        given(arguments.getOutputDirectory()).willReturn(new File("target/integrationtesting/h2 spaces"));
        given(arguments.getDatabaseType()).willReturn("src/test/resources/integrationTesting/dbTypes/h2memory");
        given(arguments.getUser()).willReturn("sa");
        given(arguments.getCatalog()).willReturn(h2MemoryRule.getConnection().getCatalog());
        given(arguments.getSchema()).willReturn(h2MemoryRule.getConnection().getSchema());
        given(arguments.getDatabaseName()).willReturn("h2 spaces");
        Config config = new Config(args);
        DatabaseMetaData databaseMetaData = sqlService.connect(config);
        Database database = new Database(config, databaseMetaData, arguments.getDatabaseName(), arguments.getCatalog(), arguments.getSchema(), null, progressListener);
        databaseService.gatheringSchemaDetails(config, database, progressListener);
        this.database = database;
    }

    @Test
    public void databaseShouldExist() {
        assertThat(database).isNotNull();
        assertThat(database.getName()).isEqualToIgnoringCase("h2 spaces");
    }

    @Test
    public void tableWithSpacesShouldExist() {
        assertThat(database.getTables()).extracting(t -> t.getName()).contains("has space");
    }
}
