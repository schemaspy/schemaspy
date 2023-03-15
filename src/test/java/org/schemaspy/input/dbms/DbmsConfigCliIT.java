package org.schemaspy.input.dbms;

import org.junit.jupiter.api.Test;
import org.schemaspy.cli.NoRowsConfigCli;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class DbmsConfigCliIT {

    @Test
    void getBuiltInDatabaseTypes() {
        assertThat(
            new DbmsConfigCli(new NoRowsConfigCli(), (name) -> new Properties())
                .getBuiltInDatabaseTypes()
        )
            .contains("mysql");
    }
}