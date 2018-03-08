package org.schemaspy.cli;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

public class PropertyFileDefaultProviderTest {

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();

    private static PropertyFileDefaultProvider propertyFileDefaultProvider;

    @BeforeClass
    public static void createPropertiesFile() throws IOException {
        File propertiesFile = temporaryFolder.newFile("schemaspy.properties");
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(propertiesFile.toPath(), StandardCharsets.UTF_8)) {
            bufferedWriter.write("schemaspy.user=humbug");
            bufferedWriter.newLine();
            bufferedWriter.write("schemaspy.sso");
            bufferedWriter.newLine();
            bufferedWriter.write("schemaspy.debug=false");
        }
        propertyFileDefaultProvider = new PropertyFileDefaultProvider(propertiesFile.getAbsolutePath());
    }

    @Test
    public void getStringValue() {
        assertThat(propertyFileDefaultProvider.getDefaultValueFor("schemaspy.user")).isEqualTo("humbug");
    }

    @Test
    public void getSSOWithOutValueShouldBeTrue() {
        assertThat(propertyFileDefaultProvider.getDefaultValueFor("schemaspy.sso")).isEqualTo(Boolean.TRUE.toString());
    }

    @Test
    public void getDebugWithValueFalseShouldBeFalse() {
        assertThat(propertyFileDefaultProvider.getDefaultValueFor("schemaspy.debug")).isEqualTo(Boolean.FALSE.toString());
    }
}