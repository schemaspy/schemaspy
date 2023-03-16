package org.schemaspy.input.dbms;

import com.beust.jcommander.JCommander;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseTypeConfigCliTest {

    @Test
    void type() {
        assertThat(
            parse(new Properties(), "-t", "myType")
                .getType()
        )
            .isEqualTo("myType");
    }

    @Test
    void typeDefault() {
        assertThat(
            parse(new Properties())
                .getType()
        )
            .isEqualTo("ora");
    }

    @Test
    void properties() {
        Properties properties = new Properties();
        properties.put("correctType", "yes");
        assertThat(
            parse(properties, "-t", "myType")
                .getProperties()
        )
            .containsEntry("correctType", "yes");

    }

    private DatabaseTypeConfig parse(Properties properties, String...args) {
        DatabaseTypeConfigCli databaseTypeConfigCli = new DatabaseTypeConfigCli((databaseType) -> databaseType.equals("myType") ? properties : new Properties());
        JCommander jCommander = JCommander.newBuilder().build();
        jCommander.addObject(databaseTypeConfigCli);
        jCommander.parse(args);
        return databaseTypeConfigCli;
    }
}