package org.schemaspy.util;

import org.junit.Test;
import org.schemaspy.Config;

import java.io.IOException;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class ConnectionURLBuilderTest {

    @Test
    public void shouldReplaceHostInConnectionSpec() throws IOException {
        Config config = new Config(
                "-t", "src/test/resources/dbtypes/onlyHost",
                "-host", "abc.com",
                "-u", "aUser");
        Properties properties = config.determineDbProperties(config.getDbType());
        ConnectionURLBuilder connectionURLBuilder = new ConnectionURLBuilder(config, properties);
        assertThat(connectionURLBuilder.build()).isEqualToIgnoringCase("abc.com");
    }

    @Test
    public void shouldReplaceHostAndOrPortWithOnlyHost() throws IOException {
        Config config = new Config(
                "-t", "src/test/resources/dbtypes/hostOptionalPort",
                "-host", "abc.com",
                "-u", "aUser");
        Properties properties = config.determineDbProperties(config.getDbType());
        ConnectionURLBuilder connectionURLBuilder = new ConnectionURLBuilder(config, properties);
        assertThat(connectionURLBuilder.build()).isEqualToIgnoringCase("abc.com");
    }

    @Test
    public void shouldReplaceHostAndOrPortWithHostAndPort() throws IOException {
        Config config = new Config(
                "-t", "src/test/resources/dbtypes/hostOptionalPort",
                "-host", "abc.com",
                "-port", "1234",
                "-u", "aUser");
        Properties properties = config.determineDbProperties(config.getDbType());
        ConnectionURLBuilder connectionURLBuilder = new ConnectionURLBuilder(config, properties);
        assertThat(connectionURLBuilder.build()).isEqualToIgnoringCase("abc.com:1234");
    }

    @Test
    public void shouldReplaceHostAndOrPortWithHostAndPortOnlyOnce() throws IOException {
        Config config = new Config(
                "-t", "src/test/resources/dbtypes/hostOptionalPort",
                "-host", "abc.com:4321",
                "-port", "1234",
                "-u", "aUser");
        Properties properties = config.determineDbProperties(config.getDbType());
        ConnectionURLBuilder connectionURLBuilder = new ConnectionURLBuilder(config, properties);
        assertThat(connectionURLBuilder.build()).isEqualToIgnoringCase("abc.com:4321");
    }

    @Test
    public void shouldReplaceHostAndOrPortWithCustomSeparator() throws IOException {
        Config config = new Config(
                "-t", "src/test/resources/dbtypes/hostOptionalPortCustomSeparator",
                "-host", "abc.com",
                "-port", "1234",
                "-u", "aUser");
        Properties properties = config.determineDbProperties(config.getDbType());
        ConnectionURLBuilder connectionURLBuilder = new ConnectionURLBuilder(config, properties);
        assertThat(connectionURLBuilder.build()).isEqualToIgnoringCase("abc.com|1234");
    }
}