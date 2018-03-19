package org.schemaspy.cli;

import com.beust.jcommander.JCommander;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class DbmsCommandLineArgumentsTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void singleArgumentWithNoValueResultsInTrue() {
        DbmsCommandLineArguments dbmsCommandLineArguments = new DbmsCommandLineArguments();
        dbmsCommandLineArguments.setArguments(Collections.singletonList("-singleArg"));
        assertThat(dbmsCommandLineArguments.getArguments().get("singleArg")).isEqualTo(Boolean.TRUE.toString());
    }

    @Test
    public void argumentNoValueAndArgumentWithValue() {
        DbmsCommandLineArguments dbmsCommandLineArguments = new DbmsCommandLineArguments();
        List<String> args = new ArrayList<>();
        args.add("-singleArg");
        args.add("-optionWithValue");
        args.add(Boolean.FALSE.toString());
        dbmsCommandLineArguments.setArguments(args);
        assertThat(dbmsCommandLineArguments.getArguments().get("singleArg")).isEqualTo(Boolean.TRUE.toString());
        assertThat(dbmsCommandLineArguments.getArguments().get("optionWithValue")).isEqualTo(Boolean.FALSE.toString());
    }

    /*@Test
    public void connPropMultipleProps() {
        DbmsCommandLineArguments dbmsCommandLineArguments = new DbmsCommandLineArguments();
        JCommander jCommander = JCommander
                .newBuilder()
                .addObject(dbmsCommandLineArguments)
                .build();
        jCommander.parse("-connProp", "useSSL=false", "-connProp", "showSQL=true");
        Properties connectionProperties = dbmsCommandLineArguments.getConnectionProperties();
        assertThat(connectionProperties.getProperty("useSSL")).isEqualTo("false");
        assertThat(connectionProperties.getProperty("showSQL")).isEqualTo("true");
    }*/

    @Test
    public void canSetPattern() {
        DbmsCommandLineArguments dbmsCommandLineArguments = new DbmsCommandLineArguments();
        JCommander jCommander = JCommander
                .newBuilder()
                .addObject(dbmsCommandLineArguments)
                .build();
        jCommander.parse("-i", "ITEMS.*");
        Pattern tableInclusions = dbmsCommandLineArguments.getTableInclusions();
        assertThat(tableInclusions.pattern()).isEqualTo("ITEMS.*");
    }

    @Test
    public void canSetProperties() throws IOException {
        File connProps = temporaryFolder.newFile("connectionProperties.properties");
        Properties origin = new Properties();
        origin.setProperty("setInFile", "true");
        try (OutputStream outputStream = new FileOutputStream(connProps)) {
            origin.store(outputStream, "canSetProperties in DbmsCommandLineArgumentsTest");
        }
        DbmsCommandLineArguments dbmsCommandLineArguments = new DbmsCommandLineArguments();
        JCommander jCommander = JCommander
                .newBuilder()
                .addObject(dbmsCommandLineArguments)
                .build();
        jCommander.parse("-connprops", connProps.getAbsolutePath());
        Properties fromDbmsCommandLineArguments = dbmsCommandLineArguments.getConnectionProperties();
        assertThat(fromDbmsCommandLineArguments.getProperty("setInFile")).isEqualTo("true");
    }

}