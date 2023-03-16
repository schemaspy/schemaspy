package org.schemaspy.input.dbms;

import com.beust.jcommander.JCommander;
import org.junit.jupiter.api.Test;
import org.schemaspy.util.DbSpecificConfig;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class ConnectionConfigCliTest {

    @Test
    void databaseName() {
        assertThat(
            parse("-db", "myDb")
                .getDatabaseName()
        )
            .isEqualTo("myDb");
    }

    @Test
    void databaseType() {
        assertThat(
            parse("-t", "myType")
                .getDatabaseType()
        )
            .isEqualTo("myType");
    }

    @Test
    void databaseTypeProperties() {
        Properties properties = new Properties();
        properties.put("correct", "true");
        assertThat(
            parse(properties, "-t", "myType")
                .getDatabaseTypeProperties()
        )
            .containsEntry("correct", "true");
    }

    @Test
    void host() {
        assertThat(
            parse("-host", "remotehost")
                .getHost()
        )
            .isEqualTo("remotehost");
    }

    @Test
    void port() {
        assertThat(
            parse("-port", "1122")
                .getPort()
        )
            .isEqualTo(1122);
    }

    @Test
    void portDefault() {
        assertThat(
            parse()
                .getPort()
        )
            .isNull();
    }

    @Test
    void user() {
        assertThat(
            parse("-u", "usr1")
                .getUser()
        )
            .isEqualTo("usr1");
    }

    @Test
    void password() {
        assertThat(
            parse("-p", "pswd123")
                .getPassword()
        )
            .isEqualTo("pswd123");
    }

    @Test
    void connectionProperties() {
        assertThat(
            parse("-connprops", "key1\\=val1:key2\\=val2;key3\\=val3")
                .getConnectionProperties()
        )
            .isEqualTo("key1\\=val1:key2\\=val2;key3\\=val3");
    }

    @Test
    void dbSpecificConfig() {
        Properties properties = new Properties();
        properties.put("dbms", "myDbms");
        properties.put("description", "just my dbms");
        properties.put("connectionSpec", "jdbc:myDbms://<hostAndPort>/<db>/<schema>");
        properties.put("host", "host for server");
        properties.put("port", "server listening port");
        properties.put("db", "database name");
        properties.put("schema", "schema to connect to");
        assertThat(
            parse(properties, "-t", "myType")
                .getDbSpecificConfig()
        )
            .isEqualTo(new DbSpecificConfig("myType", properties));
    }

    @Test
    void driverPath() {
        assertThat(
            parse("-dp", "/somepath/to/driver")
                .getDriverPath()
        )
            .isEqualTo("/somepath/to/driver");
    }

    @Test
    void withLoadSiblings() {
        assertThat(
            parse("-loadjars")
                .withLoadSiblings()
        )
            .isTrue();
    }

    @Test
    void withLoadSiblingsDefault() {
        assertThat(
            parse()
                .withLoadSiblings()
        )
            .isFalse();
    }

    @Test
    void remainingArguments() {
        assertThat(
            parse("-server", "srv01")
                .getRemainingArguments()
        )
            .containsExactly("-server", "srv01");
    }

    private ConnectionConfig parse(String...args) {
        return parse(new Properties(), args);
    }

    private ConnectionConfig parse(Properties properties, String...args) {
        DatabaseTypeConfigCli databaseTypeConfigCli = new DatabaseTypeConfigCli(((databaseType) -> databaseType.equals("myType") ? properties : new Properties()));
        ConnectionConfigCli connectionConfigCli = new ConnectionConfigCli(databaseTypeConfigCli);
        JCommander jCommander = JCommander.newBuilder().acceptUnknownOptions(true).build();
        jCommander.addObject(databaseTypeConfigCli);
        jCommander.addObject(connectionConfigCli);
        jCommander.parse(args);
        connectionConfigCli.setRemainingArguments(jCommander.getUnknownOptions());
        return connectionConfigCli;
    }

}