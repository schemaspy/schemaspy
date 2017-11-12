package org.schemaspy.input.db.driver;

import org.junit.Rule;
import org.junit.Test;
import org.schemaspy.input.db.driver.PathResolver;
import org.schemaspy.testing.Logger;
import org.schemaspy.testing.LoggingRule;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class PathResolverTest {
    private PathResolver pathResolver = new PathResolver();

    @Rule
    public LoggingRule loggingRule = new LoggingRule();

    @Test
    public void shouldIncludeFolderAndExtEndsWithAR() throws MalformedURLException {
        String driverPath = "src/test/resources/driverFolder";
        Set<URL> urls = pathResolver.resolveDriverPath(driverPath);
        Path[] expected = {
                Paths.get("src/test/resources/driverFolder/").toAbsolutePath(),
                Paths.get("src/test/resources/driverFolder/dummy.jar").toAbsolutePath(),
                Paths.get("src/test/resources/driverFolder/dummy.nar").toAbsolutePath()
        };
        assertThat(urls).extracting(u -> Paths.get(u.getPath())).containsExactlyInAnyOrder(expected);
    }

    @Test
    public void shouldLoadListOfFiles() {
        String driverPath =
                "src/test/resources/driverFolder/dummy.jar" +
                        File.pathSeparator +
                        "src/test/resources/dbtypes/onlyHost.properties";
        Set<URL> urls = pathResolver.resolveDriverPath(driverPath);
        Path[] expected = {
                Paths.get("src/test/resources/driverFolder/dummy.jar").toAbsolutePath(),
                Paths.get("src/test/resources/dbtypes/onlyHost.properties").toAbsolutePath(),
        };
        assertThat(urls).extracting(u -> Paths.get(u.getPath())).containsExactlyInAnyOrder(expected);
    }

    @Test
    public void shouldLoadSingleFile() {
        String driverPath = "src/test/resources/driverFolder/dummy.jar";
        Set<URL> urls = pathResolver.resolveDriverPath(driverPath);
        Path[] expected = {
                Paths.get("src/test/resources/driverFolder/dummy.jar").toAbsolutePath()
        };
        assertThat(urls).extracting(u -> Paths.get(u.getPath())).containsExactlyInAnyOrder(expected);
    }

    @Test
    @Logger(PathResolver.class)
    public void shouldLogMissing() {
        String driverPath = "src/test/resources/driverFolderThatDoesntExist";
        pathResolver.resolveDriverPath(driverPath);
        assertThat(loggingRule.getLog()).contains("src/test/resources/driverFolderThatDoesntExist doesn't exist");
    }
}
